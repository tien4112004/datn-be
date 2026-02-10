package com.datn.datnbe.cms.entity;

import com.datn.datnbe.cms.enums.LessonStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "lessons")
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36)
    String id;

    @Column(name = "class_id", length = 36)
    String classId;

    @Column(name = "class_name", length = 255)
    String className;

    @Column(name = "subject", nullable = false, length = 255)
    String subject;

    @Column(name = "title", nullable = false, length = 500)
    String title;

    @Column(name = "description", columnDefinition = "TEXT")
    String description;

    @Column(name = "duration")
    Integer duration;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    LessonStatus status;

    @Column(name = "notes", columnDefinition = "TEXT")
    String notes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "learning_objectives", columnDefinition = "jsonb")
    String learningObjectives;

    @Column(name = "owner_id", nullable = false, length = 36)
    String ownerId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    Date updatedAt;
}
