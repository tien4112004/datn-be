package com.datn.datnbe.ai.dto.request;

import lombok.Data;

@Data
public class RefineElementTextRequest {
    private String slideId;
    private String elementId;
    private String currentText;
    private String instruction;
    private Object slideSchema;
    private String slideType;
    private String operation;
    private String model;
    private String provider;
}
