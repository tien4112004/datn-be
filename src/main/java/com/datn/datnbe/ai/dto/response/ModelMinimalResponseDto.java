package com.datn.datnbe.ai.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Builder
@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class ModelMinimalResponseDto {
    String modelId;
    String displayName;
    boolean isEnabled;
    String provider;

}
