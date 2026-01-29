package com.datn.datnbe.ai.repository;

import com.datn.datnbe.ai.entity.CoinPricing;
import com.datn.datnbe.ai.enums.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;
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
     * Find all active pricing rules.
     *
     * @return list of active pricing rules
     */
    List<CoinPricing> findByIsActiveTrue();

    /**
     * Find pricing rule by resource type and model name.
     *
     * @param resourceType the resource type
     * @param modelName    the model name
     * @return optional containing the pricing rule if found
     */
    Optional<CoinPricing> findByResourceTypeAndModelName(ResourceType resourceType, String modelName);

    /**
     * Find default pricing rule for a resource type (where model_name is null).
     *
     * @param resourceType the resource type
     * @return optional containing the default pricing rule if found
     */
    Optional<CoinPricing> findByResourceTypeAndModelNameIsNull(ResourceType resourceType);

    /**
     * Check if a pricing rule exists for a resource type and model name combination.
     *
     * @param resourceType the resource type
     * @param modelName    the model name (can be null for default pricing)
     * @return true if pricing rule exists
     */
    boolean existsByResourceTypeAndModelName(ResourceType resourceType, String modelName);

    /**
     * Find all pricing rules by active status.
     *
     * @param isActive the active status to filter by
     * @return list of pricing rules matching the active status
     */
    List<CoinPricing> findByIsActive(Boolean isActive);

    /**
     * Find all pricing rules by resource type and active status.
     *
     * @param resourceType the resource type to filter by
     * @param isActive     the active status to filter by
     * @return list of pricing rules matching the criteria
     */
    List<CoinPricing> findByResourceTypeAndIsActive(ResourceType resourceType, Boolean isActive);
}
