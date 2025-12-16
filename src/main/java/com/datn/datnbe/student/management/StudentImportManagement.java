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

        // Step 5: Save all students
        try {
            studentRepository.saveAll(students);
            log.info("Successfully imported {} students", students.size());
            return StudentImportResponseDto.success(students.size());
        } catch (Exception e) {
            log.error("Error saving students to database", e);
            errors.add("Database error: " + e.getMessage());
            return StudentImportResponseDto.failure(errors);
        }
    }
}
