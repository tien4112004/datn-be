package com.datn.datnbe.student.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;


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

    String parentContactEmail;

    // Set by import process after user creation
    String userId;
}
