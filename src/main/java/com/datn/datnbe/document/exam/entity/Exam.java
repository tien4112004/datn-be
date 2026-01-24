package com.datn.datnbe.document.exam.entity;

import com.datn.datnbe.document.exam.entity.valueobject.QuestionOrderItem;
import com.datn.datnbe.document.exam.enums.ExamDifficulty;
import com.datn.datnbe.document.exam.enums.ExamStatus;
import com.datn.datnbe.document.exam.enums.GradeLevel;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "exams", indexes = {@Index(name = "idx_teacher_id", columnList = "teacher_id"),
        @Index(name = "idx_status", columnList = "status"), @Index(name = "idx_created_at", columnList = "created_at")})
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "exam_id")
    UUID examId;

    @Column(name = "teacher_id", nullable = false)
    UUID teacherId;

    @Column(name = "title", nullable = false, length = 255)
    String title;

    @Column(name = "description", columnDefinition = "TEXT")
    String description;

    @Column(name = "topic", nullable = false, length = 255)
    String topic;

    @Enumerated(EnumType.STRING)
    @Column(name = "grade_level", nullable = false, length = 2)
    GradeLevel gradeLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", nullable = false, length = 20)
    ExamDifficulty difficulty;

    @Column(name = "total_questions", nullable = false)
    Integer totalQuestions;

    @Column(name = "total_points", nullable = false)
    Integer totalPoints;

    @Column(name = "time_limit_minutes")
    Integer timeLimitMinutes;

    @Type(JsonType.class)
    @Column(name = "question_order", columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    QuestionOrder questionOrder = new QuestionOrder();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    ExamStatus status = ExamStatus.DRAFT;

    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (questionOrder == null) {
            questionOrder = new QuestionOrder();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionOrder {
        @Builder.Default
        private List<QuestionOrderItem> items = new ArrayList<>();
    }
}
