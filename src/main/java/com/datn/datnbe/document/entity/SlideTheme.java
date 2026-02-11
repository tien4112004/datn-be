package com.datn.datnbe.document.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "slide_themes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class SlideTheme {

    @Id
    @Column(name = "id", nullable = false, length = 50)
    String id;

    @Column(name = "name", nullable = false, length = 255)
    String name;

    @Column(name = "modifiers", length = 1000)
    String modifiers;

    @Column(name = "is_enabled", nullable = false)
    @Builder.Default
    Boolean isEnabled = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    Date createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    Date updatedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data", columnDefinition = "jsonb")
    @Builder.Default
    Map<String, Object> data = new HashMap<>();
}
