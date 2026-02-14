package com.datn.datnbe.ai.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import java.util.List;

@Data
public class ExpandCombinedTextRequest {
    private String slideId;
    private List<Object> items; // Can be strings or objects
    private String instruction;
    private JsonNode slideSchema;
    private String slideType;
    private String operation;
    private String model;
    private String provider;
}
