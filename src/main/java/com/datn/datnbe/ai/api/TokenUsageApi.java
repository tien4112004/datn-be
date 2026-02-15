package com.datn.datnbe.ai.api;

import com.datn.datnbe.ai.entity.TokenUsage;
import com.datn.datnbe.ai.dto.response.TokenUsageStatsDto;

import java.util.List;

public interface TokenUsageApi {

    void recordTokenUsage(TokenUsage tokenUsage);

    TokenUsageStatsDto getStatsWithFilters(String userId, String model, String provider, String requestType);

    List<TokenUsageStatsDto> getTokenUsageByModel(String userId);

    List<TokenUsageStatsDto> getTokenUsageByRequestType(String userId);
}
