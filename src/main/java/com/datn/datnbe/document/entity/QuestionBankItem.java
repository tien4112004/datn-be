package com.datn.datnbe.document.entity;

import com.datn.datnbe.document.entity.questiondata.Difficulty;
import com.datn.datnbe.document.entity.questiondata.FillInBlankData;
import com.datn.datnbe.document.entity.questiondata.MatchingData;
import com.datn.datnbe.document.entity.questiondata.MultipleChoiceData;
import com.datn.datnbe.document.entity.questiondata.OpenEndedData;
import com.datn.datnbe.document.entity.questiondata.QuestionType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import org.checkerframework.checker.units.qual.C;
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
public class QuestionBankItem {

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

    @Column(name = "grade")
    String grade;

    @Column(name = "chapter")
    String chapter;

    @Column(name = "subject", nullable = false)
    String subject;

    @Column(name = "context_id", length = 36)
    String contextId;

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    @JsonSubTypes({@JsonSubTypes.Type(value = OpenEndedData.class, name = "OPEN_ENDED"),
            @JsonSubTypes.Type(value = MultipleChoiceData.class, name = "MULTIPLE_CHOICE"),
            @JsonSubTypes.Type(value = MatchingData.class, name = "MATCHING"),
            @JsonSubTypes.Type(value = FillInBlankData.class, name = "FILL_IN_BLANK")})
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
