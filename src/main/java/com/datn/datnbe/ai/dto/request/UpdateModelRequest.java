package com.datn.datnbe.ai.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateModelRequest {
    @NotBlank(message = "Model name is required")
    String modelName;

    @NotBlank(message = "Display name is required")
    String displayName;

    @NotBlank(message = "Provider is required")
    String provider;
}
