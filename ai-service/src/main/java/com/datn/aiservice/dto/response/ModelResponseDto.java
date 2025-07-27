package com.datn.aiservice.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Builder
@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class ModelResponseDto {
    String modelId;
    String modelName;
    String displayName;
    boolean isEnabled;
    boolean isDefault;
}
