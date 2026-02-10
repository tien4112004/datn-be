package com.datn.datnbe.document.entity;

import com.datn.datnbe.document.entity.valueobject.MindmapNode;
import com.datn.datnbe.document.entity.valueobject.MindmapEdge;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "mindmaps")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Mindmap {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    String id;

    @Column(name = "title", nullable = false)
    String title;

    @Column(name = "description")
    String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "nodes", columnDefinition = "jsonb")
    List<MindmapNode> nodes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "edges", columnDefinition = "jsonb")
    List<MindmapEdge> edges;

    @Column(name = "created_at", nullable = false, updatable = false)
    Date createdAt;

    @Column(name = "updated_at", nullable = false)
    Date updatedAt;

    @Column(name = "thumbnail", columnDefinition = "TEXT", nullable = true)
    String thumbnail;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "extra_fields", columnDefinition = "jsonb")
    @Builder.Default
    Map<String, Object> extraFields = new HashMap<>();

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }
}
