package com.datn.datnbe.auth.dto.request;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/**
 * Request DTO for sharing a file with another user.
 * Contains the target user information and the permissions to grant.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileShareRequest {

    /**
     * The Keycloak user ID of the user to share the file with.
     * This should be the UUID of the user in Keycloak.
     */
    @NotBlank(message = "Target user ID is required")
    String targetUserId;

    /**
     * The set of permissions to grant to the target user.
     * Valid values: "read", "write"
     * Note: "share" permission is typically reserved for owners.
     */
    @NotNull(message = "Permissions are required")
    @NotEmpty(message = "At least one permission must be specified")
    Set<String> permissions;
}
