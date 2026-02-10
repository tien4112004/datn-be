package com.datn.datnbe.document.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "presentation_comments")
public class PresentationComment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36)
    String id;

    @Column(name = "presentation_id", nullable = false, length = 36)
    String presentationId;

    @Column(name = "user_id", nullable = false, length = 36)
    String userId;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    String content;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "mentioned_users", columnDefinition = "jsonb")
    @Builder.Default
    List<String> mentionedUsers = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    Date updatedAt;

    @Column(name = "deleted_at")
    Date deletedAt;
}
