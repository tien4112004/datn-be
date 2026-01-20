package com.datn.datnbe.ai.dto;

import lombok.Data;
import java.util.Map;

@Data
public class AIModificationRequest {
    private String action;
    private AIModificationContext context;
    private Map<String, Object> parameters;

    @Data
    public static class AIModificationContext {
        private String type; // 'slide', 'element', 'selection'
        private String slideId;
        private Object slideContent; // Use Object alias generic approach, usually Map or JSON Node
        private Object elementContent;
        private Object selection;
    }
}
