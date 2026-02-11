package com.datn.datnbe.cms.entity;

import com.datn.datnbe.cms.enums.LessonResourceType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "lesson_resources")
public class LessonResource {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36)
    String id;

    @Column(name = "lesson_id", nullable = false, length = 36)
    String lessonId;

    @Column(name = "name", nullable = false, length = 500)
    String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    LessonResourceType type;

    @Column(name = "url", length = 1000)
    String url;

    @Column(name = "file_path", length = 1000)
    String filePath;

    @Column(name = "description", columnDefinition = "TEXT")
    String description;

    @Column(name = "is_required")
    Boolean isRequired;

    @Column(name = "is_prepared")
    Boolean isPrepared;

    @Column(name = "uploaded_by", length = 36)
    String uploadedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    Date createdAt;
}
