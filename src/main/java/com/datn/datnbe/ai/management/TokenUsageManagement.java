package com.datn.datnbe.ai.management;

import com.datn.datnbe.ai.api.TokenUsageApi;
import com.datn.datnbe.ai.dto.response.TokenUsageStatsDto;
import com.datn.datnbe.ai.entity.TokenUsage;
import com.datn.datnbe.ai.repository.TokenUsageRepository;
import com.datn.datnbe.payment.api.PaymentApi;

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
    TokenUsageRepository tokenUsageRepo;
    PaymentApi paymentApi;

    @Override
    public void recordTokenUsage(TokenUsage tokenUsage) {
        Long existingTokenCount = tokenUsageRepo.getTotalTokenIfExisted(tokenUsage.getDocumentId());
        if (existingTokenCount != null) {
            tokenUsageRepo.updateTokenUsageWithNewData(tokenUsage.getDocumentId(),
                    tokenUsage.getTokenCount(),
                    tokenUsage.getInputTokens(),
                    tokenUsage.getOutputTokens(),
                    tokenUsage.getActualPrice(),
                    tokenUsage.getCalculatedPrice());

            paymentApi.subtractCoin(tokenUsage.getUserId(), tokenUsage.getCalculatedPrice(), tokenUsage.getRequest());
            return;
        }
        paymentApi.subtractCoin(tokenUsage.getUserId(), tokenUsage.getCalculatedPrice(), tokenUsage.getRequest());
        tokenUsageRepo.save(tokenUsage);
    }

    @Override
    public TokenUsageStatsDto getStatsWithFilters(String userId,
            String model,
            String provider,
            String requestType) {
        TokenUsageStatsDto result = tokenUsageRepo.getStatsWithFilters(userId, model, provider, requestType);
        
        if (result == null) {
            result = TokenUsageStatsDto.builder()
                    .totalTokens(0L)
                    .totalRequests(0L)
                    .totalCoin("0")
                    .totalMoney("0")
                    .build();
        }
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
