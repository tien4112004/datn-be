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
@Table(name = "submissions")
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36)
    String id;

    @Column(name = "lesson_id", nullable = false, length = 36)
    String lessonId;

    @Column(name = "student_id", nullable = false, length = 36)
    String studentId;

    @Column(name = "content", columnDefinition = "TEXT")
    String content;

    @Column(name = "media_url", length = 1000)
    String mediaUrl;

    @Column(name = "grade")
    Integer grade;

    @Column(name = "status", length = 50)
    String status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;
}
