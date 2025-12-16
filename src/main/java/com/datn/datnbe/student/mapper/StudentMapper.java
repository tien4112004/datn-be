package com.datn.datnbe.student.mapper;

import com.datn.datnbe.student.dto.request.StudentCsvRow;
import com.datn.datnbe.student.entity.Student;
import com.datn.datnbe.student.enums.StudentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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

        // Validate userId
        if (csvRow.getUserId() == null || csvRow.getUserId().isBlank()) {
            rowErrors.add(String.format("Row %d: userId is required", rowNumber));
        } else if (csvRow.getUserId().length() > 50) {
            rowErrors.add(String.format("Row %d: userId exceeds maximum length", rowNumber));
        }

        // Validate status
        StudentStatus status = parseStatus(csvRow.getStatus(), rowNumber, rowErrors);

        // Validate address (optional but has length limit)
        if (csvRow.getAddress() != null && csvRow.getAddress().length() > 255) {
            rowErrors.add(String.format("Row %d: address exceeds maximum length of 255 characters", rowNumber));
        }

        // Validate parent contact email (optional but format if provided)
        if (csvRow.getParentContactEmail() != null && !csvRow.getParentContactEmail().isBlank()) {
            if (csvRow.getParentContactEmail().length() > 100) {
                rowErrors.add(String.format("Row %d: parentContactEmail exceeds maximum length of 100 characters",
                        rowNumber));
            } else if (!isValidEmailFormat(csvRow.getParentContactEmail())) {
                rowErrors.add(String.format("Row %d: Invalid parentContactEmail format", rowNumber));
            }
        }

        if (!rowErrors.isEmpty()) {
            errors.addAll(rowErrors);
            return null;
        }

        return Student.builder()
                .userId(csvRow.getUserId())
                .enrollmentDate(csvRow.getEnrollmentDate())
                .address(csvRow.getAddress())
                .parentContactEmail(csvRow.getParentContactEmail())
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

    private boolean isValidEmailFormat(String email) {
        // Basic email validation
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}
