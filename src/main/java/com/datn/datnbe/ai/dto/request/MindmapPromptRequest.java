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
public class MindmapPromptRequest {
    String topic;
    String language;
    int maxDepth;
    int maxBranchesPerNode;
    String model;
    String provider;
}
