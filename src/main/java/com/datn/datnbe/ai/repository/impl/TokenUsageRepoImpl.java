package com.datn.datnbe.ai.repository.impl;

import com.datn.datnbe.ai.entity.TokenUsage;
import com.datn.datnbe.ai.dto.response.TokenUsageStatsDto;
import com.datn.datnbe.ai.dto.response.TokenUsageAggregatedDto;
import com.datn.datnbe.ai.repository.impl.jpa.TokenUsageJPARepo;
import com.datn.datnbe.ai.repository.interfaces.TokenUsageRepo;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Override
    public TokenUsageAggregatedDto getTokenUsageByDocumentId(String documentId) {
        List<TokenUsage> usages = tokenUsageJPARepo.findByDocumentId(documentId);

        if (usages.isEmpty()) {
            log.debug("No token usages found for documentId: {}", documentId);
            return TokenUsageAggregatedDto.builder()
                    .documentId(documentId)
                    .totalInputTokens(0L)
                    .totalOutputTokens(0L)
                    .totalTokens(0L)
                    .totalCost(BigDecimal.ZERO)
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
        Map<String, Long> requestCountByType = usages.stream()
                .collect(Collectors.groupingBy(TokenUsage::getRequest, Collectors.counting()));

        // Group by model
        Map<String, Long> requestCountByModel = usages.stream()
                .collect(Collectors.groupingBy(TokenUsage::getModel, Collectors.counting()));

        // Group by provider
        Map<String, Long> requestCountByProvider = usages.stream()
                .collect(Collectors.groupingBy(TokenUsage::getProvider, Collectors.counting()));

        // Calculate total cost (TODO: integrate with pricing config)
        BigDecimal totalCost = calculateTotalCost(usages);

        // Calculate average tokens per request
        long totalOperations = usages.size();
        long avgTokensPerRequest = totalOperations > 0 ? totalTokens / totalOperations : 0L;

        return TokenUsageAggregatedDto.builder()
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
    }

    @Override
    public List<TokenUsage> getTokenUsagesByDocumentId(String documentId) {
        return tokenUsageJPARepo.findByDocumentId(documentId);
    }

    /**
     * Calculate total cost cho list of token usages
     * TODO: Integrate với pricing configuration
     */
    private BigDecimal calculateTotalCost(List<TokenUsage> usages) {
        BigDecimal totalCost = BigDecimal.ZERO;

        for (TokenUsage usage : usages) {
            BigDecimal cost = calculateCostForUsage(usage);
            totalCost = totalCost.add(cost);
        }

        return totalCost;
    }

    private BigDecimal calculateCostForUsage(TokenUsage usage) {
        if (usage.getModel() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal inputCost = BigDecimal.ZERO;
        BigDecimal outputCost = BigDecimal.ZERO;

        // GPT-4 pricing
        if ("gpt-4".equalsIgnoreCase(usage.getModel())) {
            if (usage.getInputTokens() != null) {
                inputCost = BigDecimal.valueOf(usage.getInputTokens()).multiply(BigDecimal.valueOf(0.00003)); // $0.03 per 1K
            }
            if (usage.getOutputTokens() != null) {
                outputCost = BigDecimal.valueOf(usage.getOutputTokens()).multiply(BigDecimal.valueOf(0.00006)); // $0.06 per 1K
            }
        }
        // GPT-3.5 pricing
        else if ("gpt-3.5-turbo".equalsIgnoreCase(usage.getModel())) {
            if (usage.getInputTokens() != null) {
                inputCost = BigDecimal.valueOf(usage.getInputTokens()).multiply(BigDecimal.valueOf(0.0000005)); // $0.0005 per 1K
            }
            if (usage.getOutputTokens() != null) {
                outputCost = BigDecimal.valueOf(usage.getOutputTokens()).multiply(BigDecimal.valueOf(0.0000015)); // $0.0015 per 1K
            }
        }
        // DALL-E pricing
        else if ("dall-e-3".equalsIgnoreCase(usage.getModel())) {
            outputCost = BigDecimal.valueOf(0.02); // $0.02 per image
        }

        return inputCost.add(outputCost);
    }
}
