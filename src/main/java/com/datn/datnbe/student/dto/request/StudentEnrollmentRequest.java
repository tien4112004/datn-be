package com.datn.datnbe.student.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Request DTO for enrolling a student in a class.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StudentEnrollmentRequest {

    @NotBlank(message = "Student ID is required")
    String studentId;
}
