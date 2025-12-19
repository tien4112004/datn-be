package com.datn.datnbe.document.entity;

import com.datn.datnbe.document.entity.valueobject.Slide;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "presentations")
public class Presentation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(name = "title", nullable = false)
    String title;

    @Column(name = "thumbnail", nullable = true)
    String thumbnail;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "slides", columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    List<Slide> slides = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;

    @Column(name = "is_parsed")
    Boolean isParsed;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    Map<String, Object> metadata = new HashMap<>();

    @Column(name = "deleted_at")
    LocalDate deletedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (slides == null) {
            slides = new ArrayList<>();
        }
        if (metadata == null) {
            metadata = new HashMap<>();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @JsonAnySetter
    public void setMetadata(String key, Object value) {
        metadata.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getMetadata() {
        return metadata;
    }
}
