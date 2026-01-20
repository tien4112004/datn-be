package com.datn.datnbe.ai.dto.requests;

import lombok.Data;

@Data
public class RefineElementTextRequest {
    private String slideId;
    private String elementId;
    private String currentText; // Plain text from frontend
    private String instruction;
}
