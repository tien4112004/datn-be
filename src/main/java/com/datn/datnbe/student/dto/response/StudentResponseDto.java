package com.datn.datnbe.student.dto.response;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/**
 * Response DTO for student data.
 * Includes optional credentials (username/password) when returned from create operations.
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
    String userId;
    Date enrollmentDate;
    String address;
    String gender;
    String parentName;
    String parentPhone;
    String parentContactEmail;
    Date createdAt;
    Date updatedAt;

    // Credentials (only populated during create operations)
    String username;
    String password;
    // User profile fields (joined from auth.user_profile)
    String firstName;
    String lastName;
    String avatarUrl;
    String phoneNumber;
}
