package com.datn.aiservice.dto.request;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class SlidePromptRequest {
    String prompt;
    String language;
    String style;
    // Will handle later
    // String model;
}