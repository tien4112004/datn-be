package com.datn.datnbe.ai.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiWokerResponse {
    private String data;

    @JsonProperty("token_usage")
    private TokenUsageInfoDto tokenUsage;
}
