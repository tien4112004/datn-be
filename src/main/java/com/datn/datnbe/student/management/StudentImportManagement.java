package com.datn.datnbe.student.management;

import com.datn.datnbe.student.api.StudentImportApi;
import com.datn.datnbe.student.dto.request.StudentCsvRow;
import com.datn.datnbe.student.dto.response.StudentImportResponseDto;
import com.datn.datnbe.student.entity.Student;
import com.datn.datnbe.student.mapper.StudentMapper;
import com.datn.datnbe.student.repository.StudentRepository;
import com.datn.datnbe.student.service.CsvParserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/**
 * Management service for student import operations.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class StudentImportManagement implements StudentImportApi {

    CsvParserService csvParserService;
    StudentMapper studentMapper;
    StudentRepository studentRepository;

    @Override
    @Transactional
    public StudentImportResponseDto importStudentsFromCsv(MultipartFile file) {
        log.info("Starting student import from CSV file: {}", file.getOriginalFilename());

        // Step 1: Parse CSV file
        CsvParserService.ParseResult parseResult = csvParserService.parseStudentCsv(file);
        List<String> errors = new ArrayList<>(parseResult.errors());

        if (parseResult.rows().isEmpty() && errors.isEmpty()) {
            errors.add("No valid student data found in the CSV file");
        }

        // Step 2: Convert CSV rows to entities with validation
        List<Student> students = new ArrayList<>();
        int rowNumber = 1; // Starting after header
        for (StudentCsvRow csvRow : parseResult.rows()) {
            rowNumber++;
            Student student = studentMapper.toEntity(csvRow, rowNumber, errors);
            if (student != null) {
                students.add(student);
            }
        }

        // Step 3: Check for duplicate userIds within the CSV and in database
        if (!students.isEmpty()) {
            Set<String> seenUserIds = new HashSet<>();
            Set<String> duplicateUserIds = new HashSet<>();
            for (Student student : students) {
                if (!seenUserIds.add(student.getUserId())) {
                    duplicateUserIds.add(student.getUserId());
                }
            }
            if (!duplicateUserIds.isEmpty()) {
                errors.add(String.format("Duplicate user IDs found in CSV: %s", String.join(", ", duplicateUserIds)));
            }

            // Check for existing user IDs in database
            for (Student student : students) {
                if (studentRepository.existsByUserId(student.getUserId())) {
                    errors.add(String.format("Student already exists for user ID: %s", student.getUserId()));
                }
            }
        }

        // Step 4: If any errors, abort and return failure response
        if (!errors.isEmpty()) {
            log.warn("Student import failed with {} errors", errors.size());
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
                    .username(email)
                    .firstName(firstName)
                    .lastName(lastName)
                    .password(temporaryPassword)
                    .dateOfBirth(csvRow.getDateOfBirth())
                    .phoneNumber(csvRow.getParentPhone())
                    .role("student")
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
            log.error("Error saving students to database", e);
            errors.add("Database error: " + e.getMessage());
            return StudentImportResponseDto.failure(errors);
        }
    }
}
