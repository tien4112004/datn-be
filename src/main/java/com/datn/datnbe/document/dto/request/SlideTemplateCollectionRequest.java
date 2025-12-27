package com.datn.datnbe.document.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlideTemplateCollectionRequest {

    @Min(value = 1, message = "Page must be at least 1")
    @Builder.Default
    private Integer page = 1;

    @Min(value = 1, message = "Page size must be at least 1")
    @Builder.Default
    private Integer pageSize = 10;

    @Size(max = 100, message = "Layout type must not exceed 100 characters")
    private String layout;
}
