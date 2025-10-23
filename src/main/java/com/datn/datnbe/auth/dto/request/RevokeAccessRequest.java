package com.datn.datnbe.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevokeAccessRequest {

    @NotBlank(message = "Target user ID is required")
    private String targetUserId;
}
