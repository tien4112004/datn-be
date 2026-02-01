package com.datn.datnbe.ai.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenUsageInfoDto {
    @JsonProperty("input_tokens")
    private Long inputTokens;

    @JsonProperty("output_tokens")
    private Long outputTokens;

    @JsonProperty("total_tokens")
    private Long totalTokens;

    private String model;

    private String provider;

    @JsonProperty("price_per_token")
    private BigDecimal pricePerToken;

    @JsonProperty("total_price")
    private BigDecimal totalPrice;
}
