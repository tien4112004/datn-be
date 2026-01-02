package com.datn.datnbe.document.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PexelsImageSearchRequest {
    @NotBlank(message = "Query cannot be empty")
    private String query;

    private String orientation; // landscape, portrait, square, all
    private String locale; // en-US, pt-BR, es-ES, etc.

    @Min(1)
    private Integer page = 1;

    @Min(1)
    @Max(80)
    private Integer perPage = 50;
}
