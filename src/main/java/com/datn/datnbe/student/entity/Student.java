package com.datn.datnbe.student.entity;

import com.datn.datnbe.student.enums.Gender;
import com.datn.datnbe.student.enums.StudentStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "students")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class Student {

    @Id
    @Column(name = "id", nullable = false, length = 50)
    String id;

    @Column(name = "full_name", nullable = false, length = 255)
    String fullName;

    @Column(name = "date_of_birth")
    LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 20)
    Gender gender;

    @Column(name = "address", length = 500)
    String address;

    @Column(name = "parent_name", length = 255)
    String parentName;

    @Column(name = "parent_phone", length = 20)
    String parentPhone;

    @Column(name = "class_id", length = 50)
    String classId;

    @Column(name = "enrollment_date")
    LocalDate enrollmentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    StudentStatus status = StudentStatus.ACTIVE;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;
}
