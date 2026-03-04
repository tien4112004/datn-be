package com.datn.datnbe.ai.dto.response;

import com.datn.datnbe.ai.enums.ModelType;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Builder
@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class ModelResponseDto {
    String modelId;
    String modelName;
    String displayName;
    String provider;
    boolean isEnabled;
    boolean isDefault;
    ModelType modelType;
    Date deletedAt;
}
