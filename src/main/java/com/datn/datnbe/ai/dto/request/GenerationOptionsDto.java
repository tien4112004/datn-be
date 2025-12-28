package com.datn.datnbe.ai.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerationOptionsDto {
    private String artStyle;
    private String artStyleModifiers;
    private ModelDto imageModel;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModelDto {
        private String name;
        private String provider;
    }
}
