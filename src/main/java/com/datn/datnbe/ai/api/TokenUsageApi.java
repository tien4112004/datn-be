package com.datn.datnbe.ai.api;

import com.datn.datnbe.ai.entity.TokenUsage;
import com.datn.datnbe.ai.dto.response.TokenUsageStatsDto;
import com.datn.datnbe.ai.dto.response.TokenUsageAggregatedDto;

import java.util.List;

public interface TokenUsageApi {

    void recordTokenUsage(TokenUsage tokenUsage);

    Long getTotalTokensUsedByUser(String userId);

    Long getRequestCountByUser(String userId);

    Long getRequestCountByUserAndType(String userId, String requestType);

    TokenUsageStatsDto getTokenUsageWithFilters(String userId, String model, String provider, String requestType);

    List<TokenUsageStatsDto> getTokenUsageByModel(String userId);

    List<TokenUsageStatsDto> getTokenUsageByRequestType(String userId);

    TokenUsageAggregatedDto getTokenUsageByDocumentId(String documentId);

    List<TokenUsage> getTokenUsagesByDocumentId(String documentId);
}
