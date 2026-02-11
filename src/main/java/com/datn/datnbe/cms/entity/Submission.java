package com.datn.datnbe.cms.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import com.datn.datnbe.cms.entity.answerData.AnswerData;

import java.time.LocalDateTime;
import java.util.List;

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

    @Column(name = "student_id", nullable = false, length = 36)
    String studentId;

    @Column(name = "post_id", nullable = false, length = 36)
    String postId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "questions", nullable = false, columnDefinition = "jsonb")
    List<AnswerData> questions;

    @Column(name = "content", columnDefinition = "TEXT")
    String content;

    @Column(name = "media_url", length = 1000)
    String mediaUrl;

    @Column(name = "grade")
    Integer grade;

    @Column(name = "status", length = 50)
    String status;

    @Column(name = "assignment_id", length = 36)
    String assignmentId;

    @Column(name = "graded_by", length = 36)
    String gradedBy;

    @Column(name = "graded_at")
    LocalDateTime gradedAt;

    @Column(name = "max_score")
    Integer maxScore;

    @Column(name = "submitted_at")
    LocalDateTime submittedAt;

    @Column(name = "score")
    Double score;

    @Column(name = "feedback", columnDefinition = "TEXT")
    String feedback;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;
}
