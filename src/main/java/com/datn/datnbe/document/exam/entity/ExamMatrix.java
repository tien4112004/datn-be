package com.datn.datnbe.document.exam.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.util.Date;

/**
 * Entity for persisting exam matrices.
 * Uses the same ID as the Assignment it's attached to for easy joining.
 * The entire matrix structure is stored as JSONB.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "exam_matrices")
public class ExamMatrix {

    /**
     * ID that matches the Assignment ID this matrix is attached to.
     * This allows for easy JOIN between assignments and exam_matrices tables.
     */
    @Id
    @Column(name = "id", length = 36)
    String id;

    /**
     * Owner/teacher who created this matrix.
     */
    @Column(name = "owner_id", nullable = false, length = 36)
    String ownerId;

    /**
     * Matrix name for easy identification.
     */
    @Column(name = "name", length = 255)
    String name;

    /**
     * Subject code (e.g., "T", "TV", "TA").
     * Flattened for easy filtering and searching.
     */
    @Column(name = "subject", length = 50)
    String subject;

    /**
     * Grade level (e.g., "1", "2", "3", "4", "5").
     * Flattened for easy filtering and searching.
     */
    @Column(name = "grade", length = 10)
    String grade;

    /**
     * The entire matrix structure stored as JSONB.
     * Contains: metadata, dimensions, and matrix data.
     * Stored as JSON string.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "matrix_data", columnDefinition = "jsonb", nullable = false)
    String matrixData;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    Date updatedAt;
}
