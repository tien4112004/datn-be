package com.datn.datnbe.ai.dto.requests;

import lombok.Data;

@Data
public class RefineContentRequest {
    private Object schema;
    private String instruction;
    private RefineContext context;

    @Data
    public static class RefineContext {
        private String slideId;
        private String slideType;
    }
}
