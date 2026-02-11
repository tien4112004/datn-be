package com.datn.datnbe.document.exam.entity;

import com.datn.datnbe.document.exam.enums.ContextType;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Type;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "question_contexts", indexes = {@Index(name = "idx_context_owner_id", columnList = "owner_id"),
        @Index(name = "idx_context_type", columnList = "context_type")})
public class QuestionContext {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "context_id")
    UUID contextId;

    @Column(name = "owner_id", nullable = false)
    UUID ownerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "context_type", nullable = false, length = 20)
    ContextType contextType;

    @Column(name = "title", length = 255)
    String title;

    @Column(name = "content", columnDefinition = "TEXT")
    String content;

    @Type(JsonType.class)
    @Column(name = "metadata", columnDefinition = "jsonb")
    @Builder.Default
    Map<String, Object> metadata = new HashMap<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    Date createdAt;

    @Column(name = "updated_at", nullable = false)
    Date updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        updatedAt = new Date();
        if (metadata == null) {
            metadata = new HashMap<>();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }
}
