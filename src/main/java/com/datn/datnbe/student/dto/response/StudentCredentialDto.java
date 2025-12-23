package com.datn.datnbe.student.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * DTO containing student account credentials (username and password).
 * Used in CSV import response to provide login information for newly created students.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StudentCredentialDto {

    String studentId;

    String username;

    String password;

    String email;

    String fullName;
}
