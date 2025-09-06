package com.datn.datnbe.ai.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class OutlinePromptRequest {
    String topic;
    String language;
    String model;
    int slideCount;
    String learningObjective;
    String targetAge;
}
