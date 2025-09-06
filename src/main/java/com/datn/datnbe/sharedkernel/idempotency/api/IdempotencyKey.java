package com.datn.datnbe.sharedkernel.idempotency.api;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@Entity(name = "idempotency_key")
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class IdempotencyKey {
    @Id
    @Column(name = "key", nullable = false, unique = true)
    String key;

    @Column(name = "response_data", columnDefinition = "TEXT")
    String responseData;

    @Column(name = "status_code")
    Integer statusCode;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    IdempotencyStatus status;

    @Column(name = "expired_at", nullable = true)
    LocalDateTime expiredAt;

    @Builder.Default
    @Column(name = "retry_count", nullable = true)
    Integer retryCount = 0;
}
