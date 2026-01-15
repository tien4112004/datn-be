package com.datn.datnbe.ai.repository.impl;

import com.datn.datnbe.ai.entity.TokenUsage;
import com.datn.datnbe.ai.dto.response.TokenUsageStatsDto;
import com.datn.datnbe.ai.repository.impl.jpa.TokenUsageJPARepo;
import com.datn.datnbe.ai.repository.interfaces.TokenUsageRepo;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TokenUsageRepoImpl implements TokenUsageRepo {
    TokenUsageJPARepo tokenUsageJPARepo;

    @Override
    public void saveTokenUsage(TokenUsage tokenUsage) {
        tokenUsageJPARepo.save(tokenUsage);
        log.info("Token usage saved for user: {} - request: {} - tokens: {}",
                tokenUsage.getUserId(),
                tokenUsage.getRequest(),
                tokenUsage.getTokenCount());
    }

    @Override
    public Long getTotalTokensUsedByUser(String userId) {
        Long result = tokenUsageJPARepo.getTotalTokensUsedByUser(userId);
        return result != null ? result : 0L;
    }

    @Override
    public Long getRequestCountByUser(String userId) {
        Long result = tokenUsageJPARepo.getRequestCountByUser(userId);
        return result != null ? result : 0L;
    }

    @Override
    public Long getRequestCountByUserAndType(String userId, String requestType) {
        Long result = tokenUsageJPARepo.getRequestCountByUserAndType(userId, requestType);
        return result != null ? result : 0L;
    }

    @Override
    public com.datn.datnbe.ai.dto.response.TokenUsageStatsDto getTokenUsageWithFilters(String userId,
            String model,
            String provider,
            String requestType) {
        Long totalTokens = tokenUsageJPARepo.getTotalTokensWithFilters(userId, model, provider, requestType);
        Long totalRequests = tokenUsageJPARepo.getRequestCountWithFilters(userId, model, provider, requestType);
        return com.datn.datnbe.ai.dto.response.TokenUsageStatsDto.builder()
                .totalTokens(totalTokens != null ? totalTokens : 0L)
                .totalRequests(totalRequests != null ? totalRequests : 0L)
                .build();
    }

    @Override
    public List<TokenUsageStatsDto> getTokenUsageByModel(String userId) {
        return tokenUsageJPARepo.getTokenUsageByModel(userId);
    }

    @Override
    public List<TokenUsageStatsDto> getTokenUsageByRequestType(String userId) {
        return tokenUsageJPARepo.getTokenUsageByRequestType(userId);
    }
}
