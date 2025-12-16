package com.datn.datnbe.student.mapper;

import com.datn.datnbe.student.dto.request.StudentCsvRow;
import com.datn.datnbe.student.entity.Student;
import com.datn.datnbe.student.enums.StudentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Mapper for converting StudentCsvRow to Student entity with validation.
 */
@Component
@Slf4j
public class StudentMapper {

    /**
     * Convert a CSV row to a Student entity with validation.
     *
     * @param csvRow    the CSV row data
     * @param rowNumber the row number for error reporting
     * @param errors    list to collect validation errors
     * @return the Student entity or null if validation fails
     */
    public Student toEntity(StudentCsvRow csvRow, int rowNumber, List<String> errors) {
        List<String> rowErrors = new ArrayList<>();

        // Validate status
        StudentStatus status = parseStatus(csvRow.getStatus(), rowNumber, rowErrors);

        // Validate first name
        if (csvRow.getFirstName() == null || csvRow.getFirstName().isBlank()) {
            rowErrors.add(String.format("Row %d: firstName is required", rowNumber));
        } else if (csvRow.getFirstName().length() > 100) {
            rowErrors.add(String.format("Row %d: firstName exceeds maximum length of 100 characters", rowNumber));
        }

        // Validate last name
        if (csvRow.getLastName() == null || csvRow.getLastName().isBlank()) {
            rowErrors.add(String.format("Row %d: lastName is required", rowNumber));
        } else if (csvRow.getLastName().length() > 100) {
            rowErrors.add(String.format("Row %d: lastName exceeds maximum length of 100 characters", rowNumber));
        }

        // Validate email
        if (csvRow.getEmail() == null || csvRow.getEmail().isBlank()) {
            rowErrors.add(String.format("Row %d: email is required", rowNumber));
        } else if (!isValidEmailFormat(csvRow.getEmail())) {
            rowErrors.add(String.format("Row %d: Invalid email format: %s", rowNumber, csvRow.getEmail()));
        }

        // Validate phone format (basic validation)
        if (csvRow.getPhoneNumber() != null && !csvRow.getPhoneNumber().isBlank()
                && !isValidPhoneFormat(csvRow.getPhoneNumber())) {
            rowErrors.add(String.format("Row %d: Invalid phoneNumber format", rowNumber));
        }

        if (!rowErrors.isEmpty()) {
            errors.addAll(rowErrors);
            return null;
        }

        return Student.builder()
                .id(UUID.randomUUID().toString())
                .firstName(csvRow.getFirstName())
                .lastName(csvRow.getLastName())
                .email(csvRow.getEmail())
                .phoneNumber(csvRow.getPhoneNumber())
                .avatarUrl(csvRow.getAvatarUrl())
                .status(status != null ? status : StudentStatus.ACTIVE)
                .build();
    }

    private StudentStatus parseStatus(String statusStr, int rowNumber, List<String> errors) {
        if (statusStr == null || statusStr.isBlank()) {
            return StudentStatus.ACTIVE;
        }
        try {
            return StudentStatus.fromValue(statusStr);
        } catch (IllegalArgumentException e) {
            errors.add(String.format(
                    "Row %d: Invalid status value. Expected active/transferred/graduated/dropped, got: %s",
                    rowNumber,
                    statusStr));
            return null;
        }
    }

    private boolean isValidPhoneFormat(String phone) {
        // Allow digits, spaces, dashes, parentheses, and + for international format
        return phone.matches("^[+]?[0-9\\s\\-()]+$");
    }

    private boolean isValidEmailFormat(String email) {
        // Basic email validation
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}
