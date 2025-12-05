package com.datn.datnbe.cms.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "classes")
public class ClassEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36)
    String id;

    @Column(name = "name", nullable = false, length = 50)
    String name;

    @Column(name = "grade", nullable = false)
    Integer grade;

    @Column(name = "academic_year", nullable = false, length = 9)
    String academicYear;

    @Column(name = "current_enrollment")
    @Builder.Default
    Integer currentEnrollment = 0;

    @Column(name = "teacher_id", length = 36)
    String teacherId;

    @Column(name = "classroom", length = 100)
    String classroom;

    @Column(name = "description", columnDefinition = "TEXT")
    String description;

    @Column(name = "is_active")
    @Builder.Default
    Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @OneToOne(mappedBy = "classEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    SeatingLayout seatingLayout;
}
