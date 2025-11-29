package com.datn.datnbe.student.mapper;

import com.datn.datnbe.student.dto.request.StudentCsvRow;
import com.datn.datnbe.student.entity.Student;
import com.datn.datnbe.student.enums.Gender;
import com.datn.datnbe.student.enums.StudentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Mapper for converting StudentCsvRow to Student entity with validation.
 */
@Component
@Slf4j
public class StudentMapper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

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

        // Validate and parse fields
        LocalDate dateOfBirth = parseDate(csvRow.getDateOfBirth(), "dateOfBirth", rowNumber, rowErrors);
        Gender gender = parseGender(csvRow.getGender(), rowNumber, rowErrors);
        LocalDate enrollmentDate = parseDate(csvRow.getEnrollmentDate(), "enrollmentDate", rowNumber, rowErrors);
        StudentStatus status = parseStatus(csvRow.getStatus(), rowNumber, rowErrors);

        // Validate full name format (basic validation)
        if (csvRow.getFullName() != null && csvRow.getFullName().length() > 255) {
            rowErrors.add(String.format("Row %d: fullName exceeds maximum length of 255 characters", rowNumber));
        }

        // Validate phone format (basic validation)
        if (csvRow.getParentPhone() != null && !isValidPhoneFormat(csvRow.getParentPhone())) {
            rowErrors.add(String.format("Row %d: Invalid parentPhone format", rowNumber));
        }

        if (!rowErrors.isEmpty()) {
            errors.addAll(rowErrors);
            return null;
        }

        return Student.builder()
                .id(csvRow.getId())
                .fullName(csvRow.getFullName())
                .dateOfBirth(dateOfBirth)
                .gender(gender)
                .address(csvRow.getAddress())
                .parentName(csvRow.getParentName())
                .parentPhone(csvRow.getParentPhone())
                .classId(csvRow.getClassId())
                .enrollmentDate(enrollmentDate)
                .status(status != null ? status : StudentStatus.ACTIVE)
                .build();
    }

    private LocalDate parseDate(String dateStr, String fieldName, int rowNumber, List<String> errors) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            errors.add(String
                    .format("Row %d: Invalid %s format. Expected YYYY-MM-DD, got: %s", rowNumber, fieldName, dateStr));
            return null;
        }
    }

    private Gender parseGender(String genderStr, int rowNumber, List<String> errors) {
        if (genderStr == null || genderStr.isBlank()) {
            return null;
        }
        try {
            return Gender.fromValue(genderStr);
        } catch (IllegalArgumentException e) {
            errors.add(String
                    .format("Row %d: Invalid gender value. Expected male/female/other, got: %s", rowNumber, genderStr));
            return null;
        }
    }

    private StudentStatus parseStatus(String statusStr, int rowNumber, List<String> errors) {
        if (statusStr == null || statusStr.isBlank()) {
            return StudentStatus.ACTIVE;
        }
        try {
            return StudentStatus.fromValue(statusStr);
        } catch (IllegalArgumentException e) {
            errors.add(String.format(
                    "Row %d: Invalid status value. Expected active/inactive/graduated/transferred/suspended, got: %s",
                    rowNumber,
                    statusStr));
            return null;
        }
    }

    private boolean isValidPhoneFormat(String phone) {
        // Allow digits, spaces, dashes, parentheses, and + for international format
        return phone.matches("^[+]?[0-9\\s\\-()]+$");
    }
}
