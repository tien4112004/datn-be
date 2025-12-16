package com.datn.datnbe.student.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

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
    String userId;
    LocalDate enrollmentDate;
    String address;
    String parentContactEmail;
    String status;
}
