package com.datn.datnbe.cms.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<PostLinkedResource> postLinkedResources = new ArrayList<>();

    @Column(name = "linked_lesson_id", length = 36)
    String linkedLessonId;

    @Column(name = "assignment_id", length = 36)
    String assignmentId;

    @Column(name = "due_date")
    Date dueDate;

    @Column(name = "is_pinned")
    Boolean isPinned;

    @Column(name = "allow_comments")
    Boolean allowComments;

    @Column(name = "comment_count")
    Integer commentCount;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    Date updatedAt;
}
