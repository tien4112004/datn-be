package com.datn.datnbe.cms.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "classes")
public class ClassEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36)
    String id;

    @Column(name = "owner_id", nullable = false, length = 36)
    String ownerId;

    @Column(name = "name", nullable = false, length = 50)
    String name;

    @Column(name = "description", columnDefinition = "TEXT")
    String description;

    @Column(name = "join_code", length = 10, unique = true)
    String joinCode;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "settings", columnDefinition = "jsonb")
    String settings;

    @Column(name = "is_active")
    @Builder.Default
    Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    Date updatedAt;

    @OneToOne(mappedBy = "classEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    SeatingLayout seatingLayout;
}
