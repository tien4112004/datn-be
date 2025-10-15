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
 * DTO for creating/updating a scope-based permission in Keycloak.
 * Permissions link resources and scopes to policies.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class KeycloakPermissionDto {

    /**
     * The unique identifier of the permission (returned by Keycloak after creation)
     */
    String id;

    /**
     * The name of the permission (e.g., "read-permission" or "write-permission")
     */
    String name;

    /**
     * Description of the permission
     */
    String description;

    /**
     * The type of permission (must be "scope")
     */
    @Builder.Default
    String type = "scope";

    /**
     * The logic of the permission (POSITIVE or NEGATIVE)
     */
    @Builder.Default
    String logic = "POSITIVE";

    /**
     * The decision strategy (UNANIMOUS, AFFIRMATIVE, or CONSENSUS)
     */
    @Builder.Default
    @JsonProperty("decisionStrategy")
    String decisionStrategy = "AFFIRMATIVE";

    /**
     * Set of resource IDs this permission applies to
     */
    Set<String> resources;

    /**
     * Set of scope names this permission applies to (e.g., ["read"], ["write"])
     */
    Set<String> scopes;

    /**
     * Set of policy IDs that must be satisfied for this permission to be granted
     */
    Set<String> policies;
}
