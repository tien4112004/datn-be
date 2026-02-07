package com.datn.datnbe.ai.repository;

import com.datn.datnbe.ai.dto.response.TokenUsageStatsDto;
import com.datn.datnbe.ai.entity.TokenUsage;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface TokenUsageRepository extends JpaRepository<TokenUsage, Integer> {

    @Query("SELECT SUM(t.tokenCount) FROM TokenUsage t WHERE t.userId = :userId")
    Long getTotalTokensUsedByUser(@Param("userId") String userId);

    @Query("SELECT COUNT(t) FROM TokenUsage t WHERE t.userId = :userId")
    Long getRequestCountByUser(@Param("userId") String userId);

    @Query("SELECT COUNT(t) FROM TokenUsage t WHERE t.userId = :userId AND t.request = :requestType")
    Long getRequestCountByUserAndType(@Param("userId") String userId, @Param("requestType") String requestType);

    @Query("SELECT SUM(t.tokenCount) FROM TokenUsage t " + "WHERE (:userId IS NULL OR t.userId = :userId) "
            + "AND (:model IS NULL OR t.model = :model) " + "AND (:provider IS NULL OR t.provider = :provider) "
            + "AND (:requestType IS NULL OR t.request = :requestType)")
    Long getTotalTokensWithFilters(@Param("userId") String userId,
            @Param("model") String model,
            @Param("provider") String provider,
            @Param("requestType") String requestType);

    @Query("SELECT COUNT(t) FROM TokenUsage t " + "WHERE (:userId IS NULL OR t.userId = :userId) "
            + "AND (:model IS NULL OR t.model = :model) " + "AND (:provider IS NULL OR t.provider = :provider) "
            + "AND (:requestType IS NULL OR t.request = :requestType)")
    Long getRequestCountWithFilters(@Param("userId") String userId,
            @Param("model") String model,
            @Param("provider") String provider,
            @Param("requestType") String requestType);

    @Query("SELECT new com.datn.datnbe.ai.dto.response.TokenUsageStatsDto(t.model, SUM(t.tokenCount), COUNT(t)) "
            + "FROM TokenUsage t WHERE t.userId = :userId GROUP BY t.model")
    List<TokenUsageStatsDto> getTokenUsageByModel(@Param("userId") String userId);

    @Query("SELECT new com.datn.datnbe.ai.dto.response.TokenUsageStatsDto(t.request, SUM(t.tokenCount), COUNT(t)) "
            + "FROM TokenUsage t WHERE t.userId = :userId GROUP BY t.request")
    List<TokenUsageStatsDto> getTokenUsageByRequestType(@Param("userId") String userId);

    List<TokenUsage> findByDocumentId(String documentId);

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
