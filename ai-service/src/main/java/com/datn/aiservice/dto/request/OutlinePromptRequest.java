package com.datn.aiservice.dto.request;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class OutlinePromptRequest {
    String topic;
    String language;
    String model;
    int slideCount;
    String learningObjective;
    String targetAge;
}