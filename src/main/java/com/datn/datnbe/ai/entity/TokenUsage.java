package com.datn.datnbe.ai.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity(name = "token_usage")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "token_usage", indexes = {@Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_created_at", columnList = "created_at")})
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TokenUsage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    @Column(name = "user_id", nullable = false)
    String userId;

    @Column(name = "request", nullable = false)
    String request;

    @Column(name = "token_count")
    Long tokenCount;

    @Column(name = "model")
    String model;

    @Column(name = "provider")
    String provider;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;
}
