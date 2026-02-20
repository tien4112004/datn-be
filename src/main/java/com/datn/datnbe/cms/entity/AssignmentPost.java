package com.datn.datnbe.cms.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import com.datn.datnbe.document.entity.AssessmentMatrixCell;
import com.datn.datnbe.document.entity.AssignmentContext;
import com.datn.datnbe.document.entity.AssignmentTopic;
import com.datn.datnbe.document.entity.Question;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "assignment_post")
public class AssignmentPost {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36)
    String id;

    @Column(name = "title", nullable = false, length = 500)
    String title;

    @Column(name = "description", columnDefinition = "TEXT")
    String description;

    @Column(name = "owner_id", nullable = false, length = 36)
    String ownerId;

    @Column(name = "subject", nullable = false, length = 255)
    String subject;

    @Column(name = "grade", length = 50)
    String grade;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    Date updatedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "questions", columnDefinition = "jsonb")
    List<Question> questions;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "contexts", columnDefinition = "jsonb")
    List<AssignmentContext> contexts;

    @Column(name = "max_submissions")
    Integer maxSubmissions;

    @Column(name = "allow_retake")
    Boolean allowRetake;

    @Column(name = "shuffle_questions")
    Boolean shuffleQuestions;

    @Column(name = "show_correct_answers")
    Boolean showCorrectAnswers;

    @Column(name = "show_score_immediately")
    Boolean showScoreImmediately;

    @Column(name = "passing_score")
    Double passingScore;

    @Column(name = "available_from")
    Date availableFrom;

    @Column(name = "available_until")
    Date availableUntil;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "topics", columnDefinition = "jsonb")
    List<AssignmentTopic> topics;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "matrix_cells", columnDefinition = "jsonb")
    List<AssessmentMatrixCell> matrixCells;

    @Column(name = "source")
    String source;
}
