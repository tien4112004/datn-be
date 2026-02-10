package com.datn.datnbe.auth.entity;

import java.util.Date;

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

@Entity
@Table(name = "document_resource_mappings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DocumentResourceMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(nullable = false, unique = true)
    String documentId;

    @Column(nullable = false, unique = true)
    String keycloakResourceId;

    @Column
    @Builder.Default
    String resourceType = "presentations";

    @Column
    String resourceUri; // The exact URI used for this resource in Keycloak (e.g., /api/presentation/id)

    @Column
    String ownerId; // The user ID of the resource owner

    @Column
    String readersGroupId; // Group for users with read-only access

    @Column
    String commentersGroupId; // Group for users with read+comment access

    @Column
    String editorsGroupId; // Group for users with edit access (owner only)

    @Column
    @Builder.Default
    Boolean isPublic = false; // Whether the resource is publicly accessible

    @Column
    String publicPermission; // Permission level for public access ("read" or "comment")

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    Date createdAt;
}
