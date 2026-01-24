package com.datn.datnbe.document.exam.entity;

import com.datn.datnbe.document.exam.enums.ExamDifficulty;
import com.datn.datnbe.document.exam.enums.GradeLevel;
import com.datn.datnbe.document.exam.enums.QuestionType;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "questions", indexes = {@Index(name = "idx_question_owner_id", columnList = "owner_id"),
        @Index(name = "idx_context_id", columnList = "context_id"), @Index(name = "idx_topic", columnList = "topic"),
        @Index(name = "idx_grade_level", columnList = "grade_level"),
        @Index(name = "idx_difficulty", columnList = "difficulty"),
        @Index(name = "idx_question_filter", columnList = "owner_id, topic, grade_level")}, uniqueConstraints = {
                @UniqueConstraint(name = "idx_context_question_order", columnNames = {"context_id",
                        "question_number"})})
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "question_id")
    UUID questionId;

    @Column(name = "owner_id", nullable = false)
    UUID ownerId;

    @Column(name = "context_id")
    UUID contextId;

    @Column(name = "question_number")
    Integer questionNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 20)
    QuestionType questionType;

    @Column(name = "topic", length = 255)
    String topic;

    @Enumerated(EnumType.STRING)
    @Column(name = "grade_level", length = 2)
    GradeLevel gradeLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", length = 20)
    ExamDifficulty difficulty;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    String content;

    @Type(JsonType.class)
    @Column(name = "answers", columnDefinition = "jsonb")
    Object answers;

    @Type(JsonType.class)
    @Column(name = "correct_answer", columnDefinition = "jsonb")
    Object correctAnswer;

    @Column(name = "explanation", columnDefinition = "TEXT")
    String explanation;

    @Column(name = "default_points", nullable = false)
    @Builder.Default
    Integer defaultPoints = 1;

    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
