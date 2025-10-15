package com.datn.datnbe.auth.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
 * Entity representing a file in the system.
 * Each file has a corresponding resource in Keycloak Authorization Services
 * with associated permissions for read, write, and share operations.
 */
@Entity
@Table(name = "files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileResource {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    /**
     * The name of the file
     */
    @Column(nullable = false)
    String name;

    /**
     * The path or location of the file (could be URL, local path, etc.)
     */
    @Column(nullable = false)
    String path;

    /**
     * The size of the file in bytes
     */
    Long size;

    /**
     * MIME type of the file
     */
    String mimeType;

    /**
     * User ID of the file owner (from Keycloak)
     */
    @Column(nullable = false)
    String ownerId;

    /**
     * Username of the file owner
     */
    @Column(nullable = false)
    String ownerUsername;

    /**
     * Keycloak resource ID in Authorization Services.
     * This is the ID returned by Keycloak when the resource is registered.
     */
    @Column(unique = true)
    String keycloakResourceId;

    /**
     * Whether the file is marked as deleted (soft delete)
     */
    @Builder.Default
    @Column(nullable = false)
    Boolean deleted = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    LocalDateTime updatedAt;
}
