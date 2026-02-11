package com.datn.datnbe.student.dto.request;

import java.util.Date;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/**
 * Request DTO for creating a new student.
 * Requires user creation fields (fullName, dateOfBirth, etc.) which will be used to create a user via UserProfileAPI.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StudentCreateRequest {

    @NotBlank(message = "Full name is required")
    String fullName;

    Date dateOfBirth;

    @Pattern(regexp = "(?i)Male|Female", message = "Gender must be either 'Male' or 'Female'")
    String gender;

    @Size(max = 255, message = "Address must not exceed 255 characters")
    String address;

    @NotBlank(message = "Parent name is required")
    String parentName;

    @NotBlank(message = "Parent phone is required")
    String parentPhone;

    @Size(max = 100, message = "Parent contact email must not exceed 100 characters")
    String parentContactEmail;
}
