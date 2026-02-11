package com.datn.datnbe.ai.entity;

import java.util.Date;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity(name = "ai_result")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Integer id;

    @Column(name = "result", columnDefinition = "TEXT")
    String result;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    Date createdAt;

    @Column(name = "presentation_id", nullable = false)
    String presentationId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "generation_options", columnDefinition = "JSONB")
    String generationOptions;
}
