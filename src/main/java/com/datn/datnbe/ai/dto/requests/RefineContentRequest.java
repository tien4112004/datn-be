package com.datn.datnbe.ai.dto.requests;

import lombok.Data;

@Data
public class RefineContentRequest {
    private Object content; // Can be string or JSON object
    private String instruction;
    private RefineContext context;

    @Data
    public static class RefineContext {
        private String title;
        private String slideId;
    }
}
