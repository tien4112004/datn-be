package com.datn.datnbe.student.dto.request;

import java.util.Date;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/**
 * Request DTO for updating a student.
 * Updates both UserProfile fields (dateOfBirth, phoneNumber) and Student entity fields (address, parentContactEmail).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StudentUpdateRequest {

    @Past(message = "Date of birth must be in the past")
    Date dateOfBirth;

    String phoneNumber;

    @Size(max = 255, message = "Address must not exceed 255 characters")
    String address;

    @Size(max = 100, message = "Parent contact email must not exceed 100 characters")
    String parentContactEmail;

    String gender;

    String parentName;

    String parentPhone;

    String fullName;
}
