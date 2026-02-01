package com.datn.datnbe.ai.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * DTO aggregated token stats cho một document/presentation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenUsageAggregatedDto {

    String documentId;

    Long totalInputTokens;

    Long totalOutputTokens;

    Long totalTokens;

    BigDecimal totalCost;

    Map<String, Long> requestCountByType;

    Map<String, Long> requestCountByModel;

    Map<String, Long> requestCountByProvider;

    Long totalOperations;

    Long averageTokensPerRequest;
}
