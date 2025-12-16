package com.datn.datnbe.student.dto.response;

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
    String userId;
    LocalDate enrollmentDate;
    String address;
    String parentContactEmail;
    StudentStatus status;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
