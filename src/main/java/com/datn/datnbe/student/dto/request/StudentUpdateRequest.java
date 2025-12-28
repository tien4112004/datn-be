package com.datn.datnbe.student.dto.request;

import com.datn.datnbe.student.enums.StudentStatus;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

/**
 * Request DTO for updating a student.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StudentUpdateRequest {

    LocalDate enrollmentDate;

    @Size(max = 255, message = "Address must not exceed 255 characters")
    String address;

    @Size(max = 100, message = "Parent contact email must not exceed 100 characters")
    String parentContactEmail;

    StudentStatus status;
}
