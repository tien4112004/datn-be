package com.datn.datnbe.ai.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenUsageStatsDto {
    Long totalTokens;
    Long totalRequests;
    String model;
    String requestType;

    public TokenUsageStatsDto(String groupKey, Long totalTokens, Long totalRequests) {
        this.model = groupKey;
        this.totalTokens = totalTokens;
        this.totalRequests = totalRequests;
    }
}
