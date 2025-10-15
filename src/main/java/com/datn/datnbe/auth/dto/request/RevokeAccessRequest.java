package com.datn.datnbe.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for revoking file access from a user
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevokeAccessRequest {

    /**
     * The ID of the user whose access should be revoked
     */
    @NotBlank(message = "Target user ID is required")
    private String targetUserId;
}
