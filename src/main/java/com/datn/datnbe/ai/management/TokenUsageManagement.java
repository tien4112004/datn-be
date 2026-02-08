package com.datn.datnbe.ai.management;

import com.datn.datnbe.ai.api.TokenUsageApi;
import com.datn.datnbe.ai.dto.response.TokenUsageStatsDto;
import com.datn.datnbe.ai.dto.response.TokenUsageAggregatedDto;
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
    public TokenUsageStatsDto getTokenUsageWithFilters(String userId,
            String model,
            String provider,
            String requestType) {
        Long totalTokens = tokenUsageRepo.getTotalTokensWithFilters(userId, model, provider, requestType);
        Long totalRequests = tokenUsageRepo.getRequestCountWithFilters(userId, model, provider, requestType);
        TokenUsageStatsDto result = TokenUsageStatsDto.builder()
                .totalTokens(totalTokens != null ? totalTokens : 0L)
                .totalRequests(totalRequests != null ? totalRequests : 0L)
                .build();

        log.debug(
                "Token usage for user {} with filters - model: {}, provider: {}, requestType: {} - tokens: {}, requests: {}",
                userId,
                model,
                provider,
                requestType,
                result.getTotalTokens(),
                result.getTotalRequests());
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
        results.forEach(dto -> {
            dto.setRequestType(dto.getModel());
            dto.setModel(null);
        });
        log.debug("Token usage by request type for user {}: {} types found", userId, results.size());
        return results;
    }

    @Override
    public TokenUsageAggregatedDto getTokenUsageByDocumentId(String documentId) {
        log.debug("Fetching aggregated token usage for documentId: {}", documentId);
        // This aggregation logic was previously in the Impl. I need to bring it here or implement it in the Repo.
        // It's complex aggregation, better suited for Service layer.
        // Let's implement it here using findAllByDocumentId.

        List<TokenUsage> usages = tokenUsageRepo.findByDocumentId(documentId);

        if (usages.isEmpty()) {
            log.debug("No token usages found for documentId: {}", documentId);
            return TokenUsageAggregatedDto.builder()
                    .documentId(documentId)
                    .totalInputTokens(0L)
                    .totalOutputTokens(0L)
                    .totalTokens(0L)
                    .totalCost(java.math.BigDecimal.ZERO)
                    .totalOperations(0L)
                    .build();
        }

        // Aggregate token counts
        long totalInputTokens = usages.stream()
                .mapToLong(u -> u.getInputTokens() != null ? u.getInputTokens() : 0L)
                .sum();
        long totalOutputTokens = usages.stream()
                .mapToLong(u -> u.getOutputTokens() != null ? u.getOutputTokens() : 0L)
                .sum();
        long totalTokens = usages.stream().mapToLong(u -> u.getTokenCount() != null ? u.getTokenCount() : 0L).sum();

        // Group by request type
        java.util.Map<String, Long> requestCountByType = usages.stream()
                .collect(java.util.stream.Collectors.groupingBy(TokenUsage::getRequest,
                        java.util.stream.Collectors.counting()));

        // Group by model
        java.util.Map<String, Long> requestCountByModel = usages.stream()
                .collect(java.util.stream.Collectors.groupingBy(TokenUsage::getModel,
                        java.util.stream.Collectors.counting()));

        // Group by provider
        java.util.Map<String, Long> requestCountByProvider = usages.stream()
                .collect(java.util.stream.Collectors.groupingBy(TokenUsage::getProvider,
                        java.util.stream.Collectors.counting()));

        // Calculate total cost
        java.math.BigDecimal totalCost = calculateTotalCost(usages);

        // Calculate average tokens per request
        long totalOperations = usages.size();
        long avgTokensPerRequest = totalOperations > 0 ? totalTokens / totalOperations : 0L;

        TokenUsageAggregatedDto result = TokenUsageAggregatedDto.builder()
                .documentId(documentId)
                .totalInputTokens(totalInputTokens)
                .totalOutputTokens(totalOutputTokens)
                .totalTokens(totalTokens)
                .totalCost(totalCost)
                .requestCountByType(requestCountByType)
                .requestCountByModel(requestCountByModel)
                .requestCountByProvider(requestCountByProvider)
                .totalOperations(totalOperations)
                .averageTokensPerRequest(avgTokensPerRequest)
                .build();

        log.debug("Aggregated token usage for document {}: totalTokens: {}, totalCost: {}, operations: {}",
                documentId,
                result.getTotalTokens(),
                result.getTotalCost(),
                result.getTotalOperations());
        return result;
    }

    @Override
    public List<TokenUsage> getTokenUsagesByDocumentId(String documentId) {
        log.debug("Fetching all token usages for documentId: {}", documentId);
        List<TokenUsage> usages = tokenUsageRepo.findByDocumentId(documentId);
        log.debug("Found {} token usages for document: {}", usages.size(), documentId);
        return usages;
    }

    private java.math.BigDecimal calculateTotalCost(List<TokenUsage> usages) {
        java.math.BigDecimal totalCost = java.math.BigDecimal.ZERO;

        for (TokenUsage usage : usages) {
            java.math.BigDecimal cost = calculateCostForUsage(usage);
            totalCost = totalCost.add(cost);
        }

        return totalCost;
    }

    private java.math.BigDecimal calculateCostForUsage(TokenUsage usage) {
        if (usage.getModel() == null) {
            return java.math.BigDecimal.ZERO;
        }

        java.math.BigDecimal inputCost = java.math.BigDecimal.ZERO;
        java.math.BigDecimal outputCost = java.math.BigDecimal.ZERO;

        // GPT-4 pricing
        if ("gpt-4".equalsIgnoreCase(usage.getModel())) {
            if (usage.getInputTokens() != null) {
                inputCost = java.math.BigDecimal.valueOf(usage.getInputTokens())
                        .multiply(java.math.BigDecimal.valueOf(0.00003)); // $0.03 per 1K
            }
            if (usage.getOutputTokens() != null) {
                outputCost = java.math.BigDecimal.valueOf(usage.getOutputTokens())
                        .multiply(java.math.BigDecimal.valueOf(0.00006)); // $0.06 per 1K
            }
        }
        // GPT-3.5 pricing
        else if ("gpt-3.5-turbo".equalsIgnoreCase(usage.getModel())) {
            if (usage.getInputTokens() != null) {
                inputCost = java.math.BigDecimal.valueOf(usage.getInputTokens())
                        .multiply(java.math.BigDecimal.valueOf(0.0000005)); // $0.0005 per 1K
            }
            if (usage.getOutputTokens() != null) {
                outputCost = java.math.BigDecimal.valueOf(usage.getOutputTokens())
                        .multiply(java.math.BigDecimal.valueOf(0.0000015)); // $0.0015 per 1K
            }
        }
        // DALL-E pricing
        else if ("dall-e-3".equalsIgnoreCase(usage.getModel())) {
            outputCost = java.math.BigDecimal.valueOf(0.02); // $0.02 per image
        }

        return inputCost.add(outputCost);
    }
}
