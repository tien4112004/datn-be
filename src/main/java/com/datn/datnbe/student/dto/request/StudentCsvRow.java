package com.datn.datnbe.student.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Represents a single row from the CSV file for student import.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StudentCsvRow {
    String id;
    String fullName;
    String dateOfBirth;
    String gender;
    String address;
    String parentName;
    String parentPhone;
    String classId;
    String enrollmentDate;
    String status;
    String createdAt;
    String updatedAt;
}
