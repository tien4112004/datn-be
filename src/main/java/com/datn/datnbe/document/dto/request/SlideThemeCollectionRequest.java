package com.datn.datnbe.document.dto.request;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlideThemeCollectionRequest {

    @Min(value = 1, message = "Page must be at least 1")
    @Builder.Default
    private Integer page = 1;

    @Min(value = 1, message = "Page size must be at least 1")
    @Builder.Default
    private Integer pageSize = 10;
}
