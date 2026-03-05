package com.datn.datnbe.ai.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "teacher_system_prompts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TeacherSystemPrompt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    UUID id;

    @Column(name = "teacher_id", nullable = false, unique = true, length = 255)
    String teacherId;

    @Column(name = "prompt", nullable = false, columnDefinition = "TEXT")
    String prompt;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    Instant updatedAt = Instant.now();

    @PreUpdate
    void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
