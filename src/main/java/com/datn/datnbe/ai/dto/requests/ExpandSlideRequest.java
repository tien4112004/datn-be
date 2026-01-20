package com.datn.datnbe.ai.dto.requests;

import lombok.Data;
import java.util.Map;

@Data
public class ExpandSlideRequest {
    private Map<String, Object> currentSlide;
    private Integer count;
}
