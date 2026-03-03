package com.datn.datnbe.ai.dto.request;

import com.datn.datnbe.ai.enums.ModelType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateModelRequest {
    @NotBlank(message = "Model name is required")
    String modelName;

    @NotBlank(message = "Display name is required")
    String displayName;

    @NotBlank(message = "Provider is required")
    String provider;

    @NotNull(message = "Model type is required")
    ModelType modelType;
}
