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
    String userId;
    LocalDate enrollmentDate;
    String address;
    String parentContactEmail;
    String status;
    String createdAt;
    String updatedAt;

    String parentContactEmail;

    // Set by import process after user creation
    String userId;
}
