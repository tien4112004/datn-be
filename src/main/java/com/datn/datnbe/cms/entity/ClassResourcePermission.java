package com.datn.datnbe.cms.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "class_resource_permissions", uniqueConstraints = @UniqueConstraint(columnNames = {"class_id",
        "resource_type", "resource_id"}))
public class ClassResourcePermission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36)
    String id;

    @Column(name = "class_id", nullable = false, length = 36)
    String classId;

    @Column(name = "resource_type", nullable = false, length = 32)
    String resourceType; // "mindmap", "presentation", "assignment"

    @Column(name = "resource_id", nullable = false, length = 36)
    String resourceId;

    @Column(name = "permission_level", nullable = false, length = 16)
    @Builder.Default
    String permissionLevel = "view"; // "view" or "comment"

    @Column(name = "keycloak_group_id", length = 36)
    String keycloakGroupId;

    @Column(name = "keycloak_policy_id", length = 36)
    String keycloakPolicyId;

    @Column(name = "keycloak_permission_id", length = 36)
    String keycloakPermissionId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;
}
