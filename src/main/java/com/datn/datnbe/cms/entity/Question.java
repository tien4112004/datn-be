package com.datn.datnbe.cms.entity;

import com.datn.datnbe.cms.entity.questiondata.Difficulty;
import com.datn.datnbe.cms.entity.questiondata.QuestionType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36)
    String id;

    @Column(name = "type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    QuestionType type;

    @Column(name = "difficulty", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    Difficulty difficulty;

    @Column(name = "title", nullable = false, columnDefinition = "TEXT")
    String title;

    @Column(name = "title_image_url")
    String titleImageUrl;

    @Column(name = "explanation", columnDefinition = "TEXT")
    String explanation;

    @Column(name = "points")
    Integer points;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data", columnDefinition = "jsonb")
    Object data;

    @Column(name = "owner_id", length = 36)
    String ownerId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;
}
