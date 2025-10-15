package com.datn.datnbe.auth.dto.response;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response containing the permissions a user has on a resource
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourcePermissionResponse {

    /**
     * The resource ID (e.g., file ID)
     */
    private String resourceId;

    /**
     * The resource name in Keycloak (e.g., "file-abc123")
     */
    private String resourceName;

    /**
     * User ID who has these permissions
     */
    private String userId;

    /**
     * Set of permissions the user has (e.g., ["read", "write"])
     */
    private Set<String> permissions;

    /**
     * Whether the user has any permissions on this resource
     */
    private boolean hasAccess;
}
