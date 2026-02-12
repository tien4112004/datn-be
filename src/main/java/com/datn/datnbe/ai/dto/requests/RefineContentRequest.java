package com.datn.datnbe.ai.dto.requests;

import lombok.Data;

@Data
public class RefineContentRequest {
    private Object schema;
    private String instruction;
    private RefineContext context;
    private String operation;
    private String model;
    private String provider;

    @Data
    public static class RefineContext {
        private String slideId;
        private String slideType;
    }
}
