package com.datn.datnbe.auth.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/**
 * Simple mapping table to link your file IDs with Keycloak resource IDs.
 * This is the only table you need - no file metadata stored here.
 *
 * Usage:
 * 1. When file is created/uploaded → Save mapping (fileId, keycloakResourceId)
 * 2. When checking permission → Look up keycloakResourceId by fileId
 * 3. Call Keycloak API to check user's permission on that resource
 */
@Entity
@Table(name = "file_resource_mappings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileResourceMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    /**
     * Your file ID (from your existing file system/table)
     */
    @Column(nullable = false, unique = true)
    String fileId;

    /**
     * The Keycloak resource ID returned when you register a resource.
     * This is used to check permissions via Keycloak API.
     */
    @Column(nullable = false, unique = true)
    String keycloakResourceId;

    /**
     * The Keycloak resource name (e.g., "file-abc123")
     * Stored for convenience to avoid additional API calls
     */
    @Column(nullable = false)
    String keycloakResourceName;

    /**
     * The Keycloak group ID for file sharing
     * Created when file is first shared, contains all users with access
     */
    @Column
    String keycloakGroupId;

    /**
     * The Keycloak group name (e.g., "file-abc123-shared")
     * Stored for convenience
     */
    @Column
    String keycloakGroupName;

    /**
     * When this mapping was created
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    LocalDateTime createdAt;
}
