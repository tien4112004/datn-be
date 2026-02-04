package com.datn.datnbe.ai.repository;

import com.datn.datnbe.ai.entity.CoinPricing;
import com.datn.datnbe.ai.enums.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CoinPricingRepo extends JpaRepository<CoinPricing, String> {

    /**
     * Find all pricing rules by resource type.
     *
     * @param resourceType the resource type to filter by
     * @return list of pricing rules for the specified resource type
     */
    List<CoinPricing> findByResourceType(ResourceType resourceType);

    /**
     * Find pricing rule by resource type and model ID.
     *
     * @param resourceType the resource type
     * @param modelId      the model ID
     * @return optional containing the pricing rule if found
     */
    Optional<CoinPricing> findByResourceTypeAndModelModelId(ResourceType resourceType, Integer modelId);

    /**
     * Find default pricing rule for a resource type (where model_id is null).
     *
     * @param resourceType the resource type
     * @return optional containing the default pricing rule if found
     */
    Optional<CoinPricing> findByResourceTypeAndModelIsNull(ResourceType resourceType);

    /**
     * Check if a pricing rule exists for a resource type and model ID combination.
     *
     * @param resourceType the resource type
     * @param modelId      the model ID
     * @return true if pricing rule exists
     */
    boolean existsByResourceTypeAndModelModelId(ResourceType resourceType, Integer modelId);

    /**
     * Check if a pricing rule exists for a resource type with no model (default pricing).
     *
     * @param resourceType the resource type
     * @return true if default pricing rule exists
     */
    boolean existsByResourceTypeAndModelIsNull(ResourceType resourceType);

    @Query(value = """
            SELECT COALESCE(
                (SELECT c.base_cost FROM coin_pricing c
                 JOIN model_configuration m ON c.model_id = m.id
                 WHERE m.model_name = :model AND LOWER(m.provider) = LOWER(:provider) AND c.resource_type = :resourceType
                 LIMIT 1),
                (SELECT c.base_cost FROM coin_pricing c
                 WHERE c.resource_type like :resourceType AND c.model_id IS NULL
                 LIMIT 1)
            )
            """, nativeQuery = true)
    Long getTokenPriceInCoins(String model, String provider, String resourceType);
}
