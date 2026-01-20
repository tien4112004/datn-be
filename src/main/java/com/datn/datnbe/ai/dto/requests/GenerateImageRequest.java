package com.datn.datnbe.ai.dto.requests;

import lombok.Data;

@Data
public class GenerateImageRequest {
    private String description;
    private String style;
    private String slideId;
}
