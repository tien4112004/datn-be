package com.datn.datnbe.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Request DTO for changing user password.
 * Requires current password for verification and new password.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChangePasswordRequest {

    @NotBlank(message = "Current password is required")
    String currentPassword;

    @NotBlank(message = "New password is required")
    String newPassword;
}
