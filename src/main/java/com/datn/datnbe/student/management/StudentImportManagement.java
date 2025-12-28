package com.datn.datnbe.student.management;

import com.datn.datnbe.auth.api.UserProfileApi;
import com.datn.datnbe.auth.dto.request.SignupRequest;
import com.datn.datnbe.auth.dto.response.UserProfileResponse;
import com.datn.datnbe.student.api.StudentApi;
import com.datn.datnbe.student.api.StudentImportApi;
import com.datn.datnbe.student.dto.request.StudentCsvRow;
import com.datn.datnbe.student.dto.response.StudentCredentialDto;
import com.datn.datnbe.student.dto.response.StudentImportResponseDto;
import com.datn.datnbe.student.entity.Student;
import com.datn.datnbe.student.mapper.StudentMapper;
import com.datn.datnbe.student.repository.StudentRepository;
import com.datn.datnbe.student.service.CsvParserService;
import com.datn.datnbe.student.utils.StudentCredentialGenerator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/**
 * Management service for student import operations.
 * Implements two-phase creation:
 * 1) Create user via UserProfileAPI
 * 2) Create student linked to that user
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class StudentImportManagement implements StudentImportApi {

    CsvParserService csvParserService;
    StudentMapper studentMapper;
    StudentRepository studentRepository;
    UserProfileApi userProfileApi;
    StudentApi studentApi;

    @Override
    public StudentImportResponseDto importStudentsFromCsv(String classId, MultipartFile file) {
        log.info("Starting student import from CSV file: {} for class: {}", file.getOriginalFilename(), classId);

        // Step 1: Parse CSV file
        CsvParserService.ParseResult parseResult = csvParserService.parseStudentCsv(file);
        List<String> errors = new ArrayList<>(parseResult.errors());

        if (parseResult.rows().isEmpty() && errors.isEmpty()) {
            errors.add("No valid student data found in the CSV file");
            return StudentImportResponseDto.failure(errors);
        }

        // Return early if parsing errors exist
        if (!errors.isEmpty()) {
            return StudentImportResponseDto.failure(errors);
        }

        // Step 2: Create users first (Phase 1) - each user creation is independent
        List<StudentCredentialDto> createdCredentials = new ArrayList<>();
        List<StudentCsvRow> successfulRows = new ArrayList<>();
        int rowNumber = 0;

        for (StudentCsvRow csvRow : parseResult.rows()) {
            rowNumber++;
            try {
                StudentCredentialDto credential = createUserAndGetCredentials(csvRow, rowNumber, errors);
                if (credential != null) {
                    createdCredentials.add(credential);
                    csvRow.setUserId(credential.getStudentId());
                    successfulRows.add(csvRow);
                }
            } catch (Exception e) {
                errors.add(String.format("Row %d: Failed to create user - %s", rowNumber, e.getMessage()));
                log.error("Error creating user for row {}: {}", rowNumber, e.getMessage(), e);
            }
        }

        // Step 3: If all user creations failed, return failure
        if (successfulRows.isEmpty()) {
            log.warn("Student import failed - no users were created successfully");
            if (errors.isEmpty()) {
                errors.add("No users were created successfully");
            }
            return StudentImportResponseDto.failure(errors);
        }

        // Step 4: Convert successful CSV rows to Student entities
        List<Student> students = new ArrayList<>();
        rowNumber = 0;

        for (StudentCsvRow csvRow : successfulRows) {
            rowNumber++;
            Student student = studentMapper.toEntity(csvRow, rowNumber, errors);
            if (student != null) {
                students.add(student);
            }
        }

        // Step 5: Check for existing students in database
        // Step 5: Detect duplicate user IDs within the CSV (should fail fast)
        Set<String> seenUserIds = new HashSet<>();
        Set<String> duplicateUserIds = new HashSet<>();
        for (Student student : students) {
            if (!seenUserIds.add(student.getUserId())) {
                duplicateUserIds.add(student.getUserId());
            }
        }
        if (!duplicateUserIds.isEmpty()) {
            log.warn("Duplicate user IDs found in CSV: {}", duplicateUserIds);
            errors.add(String.format("Duplicate user IDs found in CSV: %s", String.join(", ", duplicateUserIds)));
            return StudentImportResponseDto.failure(errors);
        }

        List<Student> newStudents = new ArrayList<>();
        for (Student student : students) {
            if (studentRepository.existsByUserId(student.getUserId())) {
                log.warn("Student already exists for user ID: {}, skipping", student.getUserId());
                errors.add(String.format("Student already exists for user ID: %s (skipped)", student.getUserId()));
            } else {
                newStudents.add(student);
            }
        }

        // Step 6: Save new students
        List<Student> savedStudents = new ArrayList<>();
        if (!newStudents.isEmpty()) {
            try {
                savedStudents = studentRepository.saveAll(newStudents);
                log.info("Successfully saved {} students to database", savedStudents.size());
            } catch (Exception e) {
                log.error("Error saving students to database", e);
                errors.add("Database error while saving students: " + e.getMessage());
                return StudentImportResponseDto.failure(errors);
            }
        }

        // Step 7: Enroll students in the class
        int enrolledCount = 0;
        for (Student student : savedStudents) {
            try {
                studentApi.enrollStudent(classId, student.getId());
                enrolledCount++;
                log.info("Enrolled student {} in class {}", student.getId(), classId);
            } catch (Exception e) {
                log.error("Error enrolling student {} in class {}: {}", student.getId(), classId, e.getMessage());
                errors.add(String.format("Failed to enroll student %s in class: %s", student.getId(), e.getMessage()));
            }
        }
        log.info("Successfully enrolled {} students in class {}", enrolledCount, classId);

        // Return success with any non-fatal warnings
        if (savedStudents.isEmpty() && !errors.isEmpty()) {
            return StudentImportResponseDto.failure(errors);
        }

        return StudentImportResponseDto.builder()
            .success(true)
            .studentsCreated(savedStudents.size())
            .credentials(createdCredentials)
            .errors(errors) // Include warnings if any (empty list when none)
            .build();
    }

    /**
     * Create a user via UserProfileAPI and get credentials.
     * 
     * @param csvRow    the CSV row containing user data
     * @param rowNumber the row number for error reporting
     * @param errors    list to collect errors
     * @return StudentCredentialDto with username/password, or null if creation
     *         failed
     */
    private StudentCredentialDto createUserAndGetCredentials(StudentCsvRow csvRow, int rowNumber, List<String> errors) {
        try {
            // Validate required user fields
            if (csvRow.getFullName() == null || csvRow.getFullName().isBlank()) {
                errors.add(String.format("Row %d: fullName is required for user creation", rowNumber));
                return null;
            }

            // Parse fullName into firstName and lastName
            String[] names = csvRow.getFullName().trim().split("\\s+", 2);
            String firstName = names[0];
            String lastName = names.length > 1 ? names[1] : firstName;

            // Generate email from fullName (replace spaces with dots)
            String email = StudentCredentialGenerator.generateUsername(csvRow.getFullName(), csvRow.getDateOfBirth());

            // Generate a temporary password
            String temporaryPassword = StudentCredentialGenerator.generatePassword();

            // Create signup request
            SignupRequest signupRequest = SignupRequest.builder()
                    .email(email)
                    .firstName(firstName)
                    .lastName(lastName)
                    .password(temporaryPassword)
                    .dateOfBirth(csvRow.getDateOfBirth())
                    .phoneNumber(csvRow.getParentPhone())
                    .build();

            // Call UserProfileAPI to create user
            UserProfileResponse createdUser = userProfileApi.createUserProfile(signupRequest);

            log.info("Created user {} with email {} for row {}", createdUser.getId(), email, rowNumber);

            // Return credentials
            return StudentCredentialDto.builder()
                    .studentId(createdUser.getId())
                    .username(email)
                    .password(temporaryPassword)
                    .email(email)
                    .fullName(csvRow.getFullName())
                    .build();

        } catch (Exception e) {
            log.error("Error creating user for row {}: {}", rowNumber, e.getMessage(), e);
            errors.add(String.format("Row %d: Failed to create user - %s", rowNumber, e.getMessage()));
            return null;
        }
    }
}
