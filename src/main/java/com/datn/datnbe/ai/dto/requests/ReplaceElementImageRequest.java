package com.datn.datnbe.ai.dto.requests;

import lombok.Data;

@Data
public class ReplaceElementImageRequest {
    private String slideId;
    private String elementId;
    private String description;
    private String style;
    private Boolean matchSlideTheme;
    private Object slideSchema;
    private String slideType;
}
