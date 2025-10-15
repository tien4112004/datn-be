package com.datn.datnbe.auth.dto.keycloak;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/**
 * DTO for creating/updating a resource in Keycloak Authorization Services.
 * Represents a protected resource that users can have permissions on.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class KeycloakResourceDto {

    /**
     * The unique identifier of the resource (returned by Keycloak after creation)
     */
    @JsonProperty("_id")
    String id;

    /**
     * The name of the resource (e.g., "file-{fileId}")
     */
    String name;

    /**
     * The type of the resource (e.g., "urn:file")
     */
    String type;

    /**
     * URIs associated with the resource
     */
    Set<String> uris;

    /**
     * The scopes available for this resource (e.g., "read", "write", "share")
     */
    Set<String> scopes;

    /**
     * The owner of the resource (user ID from Keycloak)
     */
    String owner;

    /**
     * Whether the resource is owner-managed
     */
    @Builder.Default
    @JsonProperty("ownerManagedAccess")
    Boolean ownerManagedAccess = true;

    /**
     * Display name of the resource
     */
    @JsonProperty("displayName")
    String displayName;

    /**
     * Additional attributes
     */
    Object attributes;
}
