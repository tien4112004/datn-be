package com.datn.datnbe.student.dto.request;

import com.datn.datnbe.student.enums.Gender;
import com.datn.datnbe.student.enums.StudentStatus;
import jakarta.validation.constraints.NotBlank;
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

    @NotBlank(message = "Full name is required")
    @Size(max = 255, message = "Full name must not exceed 255 characters")
    String fullName;

    LocalDate dateOfBirth;

    Gender gender;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    String address;

    @Size(max = 255, message = "Parent name must not exceed 255 characters")
    String parentName;

    @Size(max = 20, message = "Parent phone must not exceed 20 characters")
    String parentPhone;

    @Size(max = 50, message = "Class ID must not exceed 50 characters")
    String classId;

    LocalDate enrollmentDate;

    StudentStatus status;
}
