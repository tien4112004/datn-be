package com.datn.datnbe.student.entity;

import com.datn.datnbe.student.enums.EnrollmentStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "class_enrollments", uniqueConstraints = {@UniqueConstraint(columnNames = {"class_id", "student_id"})})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class ClassEnrollment {

    @Id
    @Column(name = "id", nullable = false, length = 50)
    String id;

    @Column(name = "class_id", nullable = false, length = 50)
    String classId;

    @Column(name = "student_id", nullable = false, length = 50)
    String studentId;

    @Column(name = "enrolled_at")
    LocalDate enrolledAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    EnrollmentStatus status = EnrollmentStatus.ACTIVE;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;
}
