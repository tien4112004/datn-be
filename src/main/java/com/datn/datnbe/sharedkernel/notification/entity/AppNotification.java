package com.datn.datnbe.sharedkernel.notification.entity;

import com.datn.datnbe.sharedkernel.notification.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Entity
@Table(name = "app_notifications", indexes = {@Index(name = "idx_notification_user_id", columnList = "user_id"),
        @Index(name = "idx_notification_user_read", columnList = "user_id, is_read"),
        @Index(name = "idx_notification_created_at", columnList = "created_at DESC")})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private NotificationType type;

    @Column(name = "reference_id", length = 36)
    private String referenceId;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;
}
