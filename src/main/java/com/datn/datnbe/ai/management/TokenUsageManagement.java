package com.datn.datnbe.ai.management;

import com.datn.datnbe.ai.api.TokenUsageApi;
import com.datn.datnbe.ai.dto.response.TokenUsageStatsDto;
import com.datn.datnbe.ai.entity.TokenUsage;
import com.datn.datnbe.ai.repository.interfaces.TokenUsageRepo;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TokenUsageManagement implements TokenUsageApi {
    TokenUsageRepo tokenUsageRepo;

    @Override
    public void recordTokenUsage(TokenUsage tokenUsage) {
        tokenUsageRepo.saveTokenUsage(tokenUsage);
        log.debug("Recorded token usage - userId: {}, request: {}, tokens: {}", 
                tokenUsage.getUserId(), tokenUsage.getRequest(), tokenUsage.getTokenCount());
    }

    @Override
    public Long getTotalTokensUsedByUser(String userId) {
        Long total = tokenUsageRepo.getTotalTokensUsedByUser(userId);
        log.debug("Total tokens for user {}: {}", userId, total);
        return total;
    }

    @Override
    public Long getRequestCountByUser(String userId) {
        Long count = tokenUsageRepo.getRequestCountByUser(userId);
        log.debug("Total requests for user {}: {}", userId, count);
        return count;
    }

    @Override
    public Long getRequestCountByUserAndType(String userId, String requestType) {
        Long count = tokenUsageRepo.getRequestCountByUserAndType(userId, requestType);
        log.debug("Total requests for user {} with type {}: {}", userId, requestType, count);
        return count;
    }

    @Override
    public TokenUsageStatsDto getTokenUsageWithFilters(String userId, String model, String provider, String requestType) {
        TokenUsageStatsDto result = tokenUsageRepo.getTokenUsageWithFilters(userId, model, provider, requestType);
        log.debug("Token usage for user {} with filters - model: {}, provider: {}, requestType: {} - tokens: {}, requests: {}", 
                userId, model, provider, requestType, result.getTotalTokens(), result.getTotalRequests());
        return result;
    }

    @Override
    public List<TokenUsageStatsDto> getTokenUsageByModel(String userId) {
        List<TokenUsageStatsDto> results = tokenUsageRepo.getTokenUsageByModel(userId);
        log.debug("Token usage by model for user {}: {} models found", userId, results.size());
        return results;
    }

    @Override
    public List<TokenUsageStatsDto> getTokenUsageByRequestType(String userId) {
        List<TokenUsageStatsDto> results = tokenUsageRepo.getTokenUsageByRequestType(userId);
        log.debug("Token usage by request type for user {}: {} types found", userId, results.size());
        return results;
    }
}
