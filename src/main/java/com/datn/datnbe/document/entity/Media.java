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
