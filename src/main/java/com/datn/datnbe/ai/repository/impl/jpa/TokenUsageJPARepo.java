package com.datn.datnbe.ai.repository.impl.jpa;

import com.datn.datnbe.ai.entity.TokenUsage;

import jakarta.transaction.Transactional;

import com.datn.datnbe.ai.dto.response.TokenUsageStatsDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface TokenUsageJPARepo extends JpaRepository<TokenUsage, Long> {
    @Query("SELECT COALESCE(SUM(t.tokenCount), 0) FROM token_usage t WHERE t.userId = :userId")
    Long getTotalTokensUsedByUser(@Param("userId") String userId);

    @Query("SELECT COUNT(t) FROM token_usage t WHERE t.userId = :userId")
    Long getRequestCountByUser(@Param("userId") String userId);

    @Query("SELECT COUNT(t) FROM token_usage t WHERE t.userId = :userId AND t.request = :request")
    Long getRequestCountByUserAndType(@Param("userId") String userId, @Param("request") String requestType);

    @Query("SELECT COALESCE(SUM(t.tokenCount), 0) FROM token_usage t WHERE t.userId = :userId "
            + "AND (:model IS NULL OR t.model = :model) " + "AND (:provider IS NULL OR t.provider = :provider) "
            + "AND (:requestType IS NULL OR t.request = :requestType)")
    Long getTotalTokensWithFilters(@Param("userId") String userId,
            @Param("model") String model,
            @Param("provider") String provider,
            @Param("requestType") String requestType);

    @Query("SELECT COUNT(t) FROM token_usage t WHERE t.userId = :userId " + "AND (:model IS NULL OR t.model = :model) "
            + "AND (:provider IS NULL OR t.provider = :provider) "
            + "AND (:requestType IS NULL OR t.request = :requestType)")
    Long getRequestCountWithFilters(@Param("userId") String userId,
            @Param("model") String model,
            @Param("provider") String provider,
            @Param("requestType") String requestType);

    @Query("SELECT new com.datn.datnbe.ai.dto.response.TokenUsageStatsDto(COALESCE(SUM(t.tokenCount), 0), null, t.model, null) "
            + "FROM token_usage t WHERE t.userId = :userId GROUP BY t.model ORDER BY COALESCE(SUM(t.tokenCount), 0) DESC")
    List<TokenUsageStatsDto> getTokenUsageByModel(@Param("userId") String userId);

    @Query("SELECT new com.datn.datnbe.ai.dto.response.TokenUsageStatsDto(COALESCE(SUM(t.tokenCount), 0), null, null, t.request) "
            + "FROM token_usage t WHERE t.userId = :userId AND t.request != 'IMAGE_GENERATION' "
            + "GROUP BY t.request ORDER BY COALESCE(SUM(t.tokenCount), 0) DESC")
    List<TokenUsageStatsDto> getTokenUsageByRequestType(@Param("userId") String userId);

    /**
     * Get all token usages for a specific document (presentation, etc.)
     */
    List<TokenUsage> findByDocumentId(@Param("documentId") String documentId);

    @Query(value = """
            SELECT t.token_count
            FROM token_usage t
            WHERE t.document_id = :documentId
            """, nativeQuery = true)
    Long getTotalTokenIfExisted(@Param("documentId") String documentId);

    @Query(value = """
            Update token_usage
            SET token_count = :tokenCount,
                input_tokens = :inputTokens,
                output_tokens = :outputTokens,
                actual_price = :actualPrice,
                calculated_price = calculated_price + :calculatedPrice
            WHERE document_id = :documentId
            """, nativeQuery = true)
    @Modifying
    @Transactional
    void updateTokenUsageWithNewData(@Param("documentId") String documentId,
            @Param("tokenCount") Long tokenCount,
            @Param("inputTokens") Long inputTokens,
            @Param("outputTokens") Long outputTokens,
            @Param("actualPrice") BigDecimal actualPrice,
            @Param("calculatedPrice") Long calculatedPrice);
}
