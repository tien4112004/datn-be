package com.datn.datnbe.student.dto.request;

import com.datn.datnbe.student.enums.StudentStatus;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

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

    @Size(max = 100, message = "First name must not exceed 100 characters")
    String firstName;

    @Size(max = 100, message = "Last name must not exceed 100 characters")
    String lastName;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    String phoneNumber;

    String avatarUrl;

    StudentStatus status;
}
