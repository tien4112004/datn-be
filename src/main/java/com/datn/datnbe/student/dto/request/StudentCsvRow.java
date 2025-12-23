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
    String fullName;
    LocalDate dateOfBirth;
    String gender;
    String parentName;
    String parentPhone;

    String address;
    String classId;
    LocalDate enrollmentDate;
    String status;

    String parentContactEmail;

    // Set by import process after user creation
    String userId;
}
