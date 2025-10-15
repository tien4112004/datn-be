package com.datn.datnbe.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to register a file in Keycloak Authorization Services.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileRegistrationRequest {

    @NotBlank(message = "File ID is required")
    private String fileId;

    @NotBlank(message = "File name is required")
    private String fileName;

    /**
     * Resource type that determines the API path.
     * Examples: "files", "presentations", "documents"
     * Default: "files"
     */
    @Builder.Default
    private String resourceType = "files";

    /**
     * Owner's Keycloak user ID.
     * If not provided, will use the authenticated user's ID from JWT.
     */
    private String ownerId;
}
