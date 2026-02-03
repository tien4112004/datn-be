package com.datn.datnbe.document.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "matrix_templates")
public class MatrixTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36)
    String id;

    @Column(name = "title", nullable = false, length = 500)
    String title;

    @Column(name = "grade", length = 50)
    String grade;

    @Column(name = "subject", length = 255)
    String subject;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dimensions", columnDefinition = "jsonb")
    MatrixDimensions dimensions;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "matrix", columnDefinition = "jsonb")
    List<List<List<String>>> matrix;

    @Column(name = "total_questions")
    Integer totalQuestions;

    @Column(name = "total_points")
    Double totalPoints;
}
