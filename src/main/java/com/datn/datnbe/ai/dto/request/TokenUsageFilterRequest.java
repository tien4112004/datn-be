package com.datn.datnbe.ai.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TokenUsageFilterRequest {

    String model;

    String provider;

    String requestType;
}
