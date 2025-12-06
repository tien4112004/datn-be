package com.datn.datnbe.document.entity;

import com.datn.datnbe.document.entity.valueobject.MindmapNode;
import com.datn.datnbe.document.entity.valueobject.MindmapEdge;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "mindmaps")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class Mindmap {

    @Id
    @UuidGenerator
    @Column(name = "id", columnDefinition = "UUID")
    UUID id;

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

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;

}
