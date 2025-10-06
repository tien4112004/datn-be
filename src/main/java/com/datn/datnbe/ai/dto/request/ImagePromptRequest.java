package com.datn.datnbe.ai.dto.request;

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
public class ImagePromptRequest {
    String artStyle;
    String artDescription;
    String themeStyle;
    String themeDescription;
    @Builder.Default
    String aspectRatio = "1:1";
    String model;
    String provider;
    String prompt;
}
