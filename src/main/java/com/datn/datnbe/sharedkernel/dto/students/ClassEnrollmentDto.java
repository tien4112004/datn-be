package com.datn.datnbe.sharedkernel.dto.students;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

/**
 * DTO for class enrollment information.
 * Used for cross-module communication without exposing internal entity.
 */
@Value
@Builder
public class ClassEnrollmentDto {
    Long enrollmentId;
    String classId;
    String studentId;
    String userId; // The Keycloak user ID associated with the student
    String status;
    Instant enrolledAt;
}
