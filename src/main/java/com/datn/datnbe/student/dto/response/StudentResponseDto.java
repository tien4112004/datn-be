package com.datn.datnbe.student.dto.response;

import com.datn.datnbe.student.enums.Role;
import com.datn.datnbe.student.enums.StudentStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

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
    String firstName;
    String lastName;
    String email;
    String phoneNumber;
    String avatarUrl;
    Role role;
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
