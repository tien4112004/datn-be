package com.datn.datnbe.student.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

/**
 * Request DTO for creating a new student.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StudentCreateRequest {

    @NotBlank(message = "User ID is required")
    String userId;

    LocalDate enrollmentDate;

    @Size(max = 255, message = "Address must not exceed 255 characters")
    String address;

    @Size(max = 100, message = "Parent contact email must not exceed 100 characters")
    String parentContactEmail;
}
