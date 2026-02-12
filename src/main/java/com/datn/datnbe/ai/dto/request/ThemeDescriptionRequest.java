package com.datn.datnbe.ai.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThemeDescriptionRequest {
    @NotBlank(message = "Primary color is required")
    private String primaryColor;

    @NotBlank(message = "Background color is required")
    private String backgroundColor;

    @NotBlank(message = "Text color is required")
    private String textColor;
}
