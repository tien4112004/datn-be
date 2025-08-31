package com.datn.datnbe.ai.dto.request;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class ImageGenerationRequest {
    String prompt;
    @Builder.Default
    Integer sampleCount = 1;
    @Builder.Default
    String aspectRatio = "1:1";
    @Builder.Default
    String safetyFilterLevel = "block_some";
    @Builder.Default
    String personGeneration = "allow_adult";
    Integer seed;
    @Nullable
    Boolean addWatermark = null;
}
