package com.datn.datnbe.ai.repository.interfaces;

import com.datn.datnbe.ai.entity.TokenUsage;
import com.datn.datnbe.ai.dto.response.TokenUsageStatsDto;
import com.datn.datnbe.ai.dto.response.TokenUsageAggregatedDto;
import java.util.List;

public interface TokenUsageRepo {
    /**
     * Save a token usage record
     *
     * @param tokenUsage the TokenUsage entity to save
     */
    void saveTokenUsage(TokenUsage tokenUsage);

    /**
     * Get total tokens used by a user
     *
     * @param userId the ID of the user
     * @return total token count
     */
    Long getTotalTokensUsedByUser(String userId);

    /**
     * Get total request count by a user
     *
     * @param userId the ID of the user
     * @return total request count
     */
    Long getRequestCountByUser(String userId);

    /**
     * Get total request count by user and request type
     *
     * @param userId the ID of the user
     * @param requestType the type of request
     * @return total request count for the specified type
     */
    Long getRequestCountByUserAndType(String userId, String requestType);

    /**
     * Get token usage stats with flexible filtering
     * Returns both total tokens and request count
     *
     * @param userId the ID of the user
     * @param model optional model filter
     * @param provider optional provider filter
     * @param requestType optional request type filter
     * @return TokenUsageStatsDto containing both totalTokens and totalRequests
     */
    com.datn.datnbe.ai.dto.response.TokenUsageStatsDto getTokenUsageWithFilters(String userId,
            String model,
            String provider,
            String requestType);

    /**
     * Get token usage grouped by model
     *
     * @param userId the ID of the user
     * @return list of TokenUsageStatsDto with model and total tokens
     */
    List<TokenUsageStatsDto> getTokenUsageByModel(String userId);

    /**
     * Get token usage grouped by request type (excluding IMAGE_GENERATION)
     *
     * @param userId the ID of the user
     * @return list of TokenUsageStatsDto with request type and total tokens
     */
    List<TokenUsageStatsDto> getTokenUsageByRequestType(String userId);

    /**
     * Get aggregated token usage stats for a document (presentation, etc.)
     * Tính tổng tokens, cost, breakdown by type/model/provider
     *
     * @param documentId the document ID (presentationId, etc.)
     * @return TokenUsageAggregatedDto with aggregated stats
     */
    TokenUsageAggregatedDto getTokenUsageByDocumentId(String documentId);

    /**
     * Get all token usages for a specific document
     *
     * @param documentId the document ID
     * @return list of TokenUsage entities
     */
    List<TokenUsage> getTokenUsagesByDocumentId(String documentId);
}
