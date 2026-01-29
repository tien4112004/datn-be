package com.datn.datnbe.ai.presentation;

import com.datn.datnbe.ai.api.CoinPricingApi;
import com.datn.datnbe.ai.dto.request.CoinPricingCreateRequest;
import com.datn.datnbe.ai.dto.request.CoinPricingUpdateRequest;
import com.datn.datnbe.ai.dto.response.CoinPricingResponseDto;
import com.datn.datnbe.ai.enums.ResourceType;
import com.datn.datnbe.ai.enums.UnitType;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/admin/coin-pricing")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
        RequestMethod.DELETE, RequestMethod.OPTIONS})
public class AdminCoinPricingController {

    CoinPricingApi coinPricingApi;

    /**
     * Get all coin pricing configurations with optional filtering.
     *
     * @param resourceType optional filter by resource type
     * @param isActive     optional filter by active status
     * @return list of coin pricing configurations
     */
    @GetMapping
    public ResponseEntity<AppResponseDto<List<CoinPricingResponseDto>>> getAllPricing(
            @RequestParam(name = "resourceType", required = false) ResourceType resourceType,
            @RequestParam(name = "isActive", required = false) Boolean isActive) {
        log.info("Fetching all coin pricing configurations with resourceType={}, isActive={}", resourceType, isActive);
        var pricingList = coinPricingApi.getAllPricing(resourceType, isActive);
        return ResponseEntity.ok(AppResponseDto.success(pricingList));
    }

    /**
     * Get a specific coin pricing configuration by ID.
     *
     * @param id the pricing configuration ID
     * @return the pricing configuration
     */
    @GetMapping("/{id}")
    public ResponseEntity<AppResponseDto<CoinPricingResponseDto>> getPricingById(@PathVariable String id) {
        log.info("Fetching coin pricing configuration with ID: {}", id);
        var pricing = coinPricingApi.getPricingById(id);
        return ResponseEntity.ok(AppResponseDto.success(pricing));
    }

    /**
     * Create a new coin pricing configuration.
     *
     * @param request the create request
     * @return the created pricing configuration
     */
    @PostMapping
    public ResponseEntity<AppResponseDto<CoinPricingResponseDto>> createPricing(
            @Valid @RequestBody CoinPricingCreateRequest request) {
        log.info("Creating coin pricing configuration for resourceType={}, modelName={}",
                request.getResourceType(),
                request.getModelName());
        var pricing = coinPricingApi.createPricing(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(AppResponseDto.success(pricing));
    }

    /**
     * Update an existing coin pricing configuration.
     *
     * @param id      the pricing configuration ID
     * @param request the update request
     * @return the updated pricing configuration
     */
    @PutMapping("/{id}")
    public ResponseEntity<AppResponseDto<CoinPricingResponseDto>> updatePricing(@PathVariable String id,
            @Valid @RequestBody CoinPricingUpdateRequest request) {
        log.info("Updating coin pricing configuration with ID: {}", id);
        var pricing = coinPricingApi.updatePricing(id, request);
        return ResponseEntity.ok(AppResponseDto.success(pricing));
    }

    /**
     * Delete a coin pricing configuration (soft delete).
     *
     * @param id the pricing configuration ID
     * @return success response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<AppResponseDto<Void>> deletePricing(@PathVariable String id) {
        log.info("Deleting coin pricing configuration with ID: {}", id);
        coinPricingApi.deletePricing(id);
        return ResponseEntity.ok(AppResponseDto.success());
    }

    /**
     * Get all available resource types.
     *
     * @return list of resource types with their display names
     */
    @GetMapping("/resource-types")
    public ResponseEntity<AppResponseDto<List<Map<String, String>>>> getResourceTypes() {
        log.info("Fetching all resource types");
        var resourceTypes = Arrays.stream(ResourceType.values())
                .map(type -> Map.of("value", type.name(), "label", type.getDisplayName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(AppResponseDto.success(resourceTypes));
    }

    /**
     * Get all available unit types.
     *
     * @return list of unit types with their display names
     */
    @GetMapping("/unit-types")
    public ResponseEntity<AppResponseDto<List<Map<String, String>>>> getUnitTypes() {
        log.info("Fetching all unit types");
        var unitTypes = Arrays.stream(UnitType.values())
                .map(type -> Map.of("value", type.name(), "label", type.getDisplayName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(AppResponseDto.success(unitTypes));
    }
}
