package com.datn.datnbe.cms.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36)
    String id;

    @Column(name = "class_id", nullable = false, length = 36)
    String classId;

    @Column(name = "author_id", nullable = false, length = 36)
    String authorId;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    String content;

    @Column(name = "type", length = 32)
    String type;

    @Column(name = "attachments")
    String[] attachments;

    @Column(name = "linked_resource_ids")
    String[] linkedResourceIds;

    @Column(name = "linked_lesson_id", length = 36)
    String linkedLessonId;

    @Column(name = "is_pinned")
    Boolean isPinned;

    @Column(name = "allow_comments")
    Boolean allowComments;

    @Column(name = "comment_count")
    Integer commentCount;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;
}
