package com.datn.datnbe.ai.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

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
    String totalCoin;
    String totalMoney;

    public TokenUsageStatsDto(String groupKey, Long totalTokens, Long totalRequests) {
        this.model = groupKey;
        this.totalTokens = totalTokens;
        this.totalRequests = totalRequests;
    }

    // Constructor for model grouping with coin and money
    public TokenUsageStatsDto(String model, Long totalTokens, Long totalRequests, Long totalCoin,
            BigDecimal totalMoney) {
        this.model = model;
        this.totalTokens = totalTokens;
        this.totalRequests = totalRequests;
        this.totalCoin = totalCoin != null ? String.valueOf(totalCoin) : null;
        this.totalMoney = totalMoney != null ? totalMoney.toString() : null;
    }

    // Constructor for aggregated stats (no grouping)
    public TokenUsageStatsDto(Long totalTokens, Long totalRequests, Long totalCoin, BigDecimal totalMoney) {
        this.totalTokens = totalTokens;
        this.totalRequests = totalRequests;
        this.totalCoin = totalCoin != null ? String.valueOf(totalCoin) : null;
        this.totalMoney = totalMoney != null ? totalMoney.toString() : null;
    }
}
