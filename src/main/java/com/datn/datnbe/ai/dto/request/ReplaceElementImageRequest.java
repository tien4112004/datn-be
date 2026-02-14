package com.datn.datnbe.ai.dto.request;

import lombok.Data;

@Data
public class ReplaceElementImageRequest {
    private String slideId;
    private String elementId;
    private String description;
    private String style;
    private String themeDescription;
    private String artDescription;
    private Object slideSchema;
    private String slideType;
    private String model;
    private String provider;
}
