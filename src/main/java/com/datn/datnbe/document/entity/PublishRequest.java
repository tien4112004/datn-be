package com.datn.datnbe.document.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "publish_requests")
public class PublishRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36)
    String id;

    @Column(name = "question_id", nullable = false, length = 36)
    String questionId;

    @Column(name = "requester_id", nullable = false, length = 36)
    String requesterId;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    PublishRequestStatus status;

    @Column(name = "is_deleted", nullable = false)
    Boolean isDeleted;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    Date updatedAt;

    @Column(name = "deleted_at")
    Date deletedAt;

    public enum PublishRequestStatus {
        PENDING, APPROVED, REJECTED
    }
}
