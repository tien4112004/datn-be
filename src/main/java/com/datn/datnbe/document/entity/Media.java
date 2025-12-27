package com.datn.datnbe.document.entity;

import com.datn.datnbe.sharedkernel.enums.MediaType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "medias")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Media {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "original_filename", nullable = false)
    String originalFilename;

    @Column(name = "storage_key", nullable = false, unique = true)
    String storageKey;

    @Column(name = "cdn_url", nullable = false)
    String cdnUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false)
    MediaType mediaType;

    @Column(name = "file_size")
    Long fileSize;

    @Column(name = "content_type")
    String contentType;

    @Column(name = "owner_id", nullable = true, length = 36)
    String ownerId;

    @Column(name = "is_generated", nullable = false)
    @Builder.Default
    Boolean isGenerated = false;

    @Column(name = "presentation_id", length = 255)
    String presentationId;

    @Column(name = "prompt", columnDefinition = "TEXT")
    String prompt;

    @Column(name = "model", length = 100)
    String model;

    @Column(name = "provider", length = 50)
    String provider;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
