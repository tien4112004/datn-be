package com.datn.datnbe.student.dto.request;

import java.time.LocalDate;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

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

    String parentContactEmail;

    // Set by import process after user creation
    String userId;
}
