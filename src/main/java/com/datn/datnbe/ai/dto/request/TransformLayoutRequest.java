package com.datn.datnbe.ai.dto.request;

import lombok.Data;
import java.util.Map;

@Data
public class TransformLayoutRequest {
    private Map<String, Object> currentSchema;
    private String targetType;
    private String model;
    private String provider;
}
