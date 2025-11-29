package com.datn.datnbe.student.dto.response;

import com.datn.datnbe.student.enums.Gender;
import com.datn.datnbe.student.enums.StudentStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for student data.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StudentResponseDto {

    String id;
    String fullName;
    LocalDate dateOfBirth;
    Gender gender;
    String address;
    String parentName;
    String parentPhone;
    String classId;
    LocalDate enrollmentDate;
    StudentStatus status;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    // Credentials (only populated during create operations)
    String username;
    String password;
    // User profile fields (joined from auth.user_profile)
    String firstName;
    String lastName;
    String avatarUrl;
    String phoneNumber;
}
