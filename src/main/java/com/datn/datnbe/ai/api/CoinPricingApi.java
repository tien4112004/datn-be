package com.datn.datnbe.ai.api;

import com.datn.datnbe.ai.dto.request.CoinPricingCreateRequest;
import com.datn.datnbe.ai.dto.request.CoinPricingUpdateRequest;
import com.datn.datnbe.ai.dto.response.CoinPricingResponseDto;
import com.datn.datnbe.ai.enums.ResourceType;

import java.util.List;

/**
 * API interface for coin pricing management.
 * Provides CRUD operations for managing AI feature pricing configurations.
 */
public interface CoinPricingApi {

    /**
     * Get all pricing configurations with optional filtering.
     *
     * @param resourceType optional filter by resource type
     * @return list of pricing configurations
     */
    List<CoinPricingResponseDto> getAllPricing(ResourceType resourceType);

    /**
     * Get a specific pricing configuration by ID.
     *
     * @param id the pricing configuration ID
     * @return the pricing configuration
     */
    CoinPricingResponseDto getPricingById(String id);

    /**
     * Create a new pricing configuration.
     *
     * @param request the create request containing pricing details
     * @return the created pricing configuration
     */
    CoinPricingResponseDto createPricing(CoinPricingCreateRequest request);

    /**
     * Update an existing pricing configuration.
     *
     * @param id      the pricing configuration ID
     * @param request the update request containing fields to update
     * @return the updated pricing configuration
     */
    CoinPricingResponseDto updatePricing(String id, CoinPricingUpdateRequest request);

    /**
     * Delete a pricing configuration (soft delete).
     *
     * @param id the pricing configuration ID
     */
    void deletePricing(String id);

    Long getTokenPriceInCoins(String model, String provider, String resourceType);
}
