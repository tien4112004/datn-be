package com.datn.datnbe.ai.management;

import com.datn.datnbe.ai.api.CoinPricingApi;
import com.datn.datnbe.ai.dto.request.CoinPricingCreateRequest;
import com.datn.datnbe.ai.dto.request.CoinPricingUpdateRequest;
import com.datn.datnbe.ai.dto.response.CoinPricingResponseDto;
import com.datn.datnbe.ai.entity.CoinPricing;
import com.datn.datnbe.ai.enums.ResourceType;
import com.datn.datnbe.ai.repository.CoinPricingRepo;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class CoinPricingManagement implements CoinPricingApi {

    CoinPricingRepo coinPricingRepo;

    @Override
    public List<CoinPricingResponseDto> getAllPricing(ResourceType resourceType, Boolean isActive) {
        List<CoinPricing> pricingList;

        if (resourceType != null && isActive != null) {
            pricingList = coinPricingRepo.findByResourceTypeAndIsActive(resourceType, isActive);
        } else if (resourceType != null) {
            pricingList = coinPricingRepo.findByResourceType(resourceType);
        } else if (isActive != null) {
            pricingList = coinPricingRepo.findByIsActive(isActive);
        } else {
            pricingList = coinPricingRepo.findAll();
        }

        return pricingList.stream().map(this::toResponseDto).toList();
    }

    @Override
    public CoinPricingResponseDto getPricingById(String id) {
        CoinPricing pricing = coinPricingRepo.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COIN_PRICING_NOT_FOUND));

        return toResponseDto(pricing);
    }

    @Override
    @Transactional
    public CoinPricingResponseDto createPricing(CoinPricingCreateRequest request) {
        // Check for duplicate resource_type + model_name combination
        if (coinPricingRepo.existsByResourceTypeAndModelName(request.getResourceType(), request.getModelName())) {
            throw new AppException(ErrorCode.COIN_PRICING_ALREADY_EXISTS);
        }

        CoinPricing pricing = CoinPricing.builder()
                .resourceType(request.getResourceType())
                .modelName(request.getModelName())
                .baseCost(request.getBaseCost())
                .unitType(request.getUnitType())
                .unitMultiplier(request.getUnitMultiplier())
                .description(request.getDescription())
                .isActive(request.getIsActive())
                .build();

        CoinPricing saved = coinPricingRepo.save(pricing);
        log.info("Created coin pricing: {} for resource type: {}, model: {}",
                saved.getId(),
                saved.getResourceType(),
                saved.getModelName());

        return toResponseDto(saved);
    }

    @Override
    @Transactional
    public CoinPricingResponseDto updatePricing(String id, CoinPricingUpdateRequest request) {
        CoinPricing pricing = coinPricingRepo.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COIN_PRICING_NOT_FOUND));

        if (request.getBaseCost() != null) {
            pricing.setBaseCost(request.getBaseCost());
        }
        if (request.getUnitType() != null) {
            pricing.setUnitType(request.getUnitType());
        }
        if (request.getUnitMultiplier() != null) {
            pricing.setUnitMultiplier(request.getUnitMultiplier());
        }
        if (request.getDescription() != null) {
            pricing.setDescription(request.getDescription());
        }
        if (request.getIsActive() != null) {
            pricing.setIsActive(request.getIsActive());
        }

        CoinPricing updated = coinPricingRepo.save(pricing);
        log.info("Updated coin pricing: {}", updated.getId());

        return toResponseDto(updated);
    }

    @Override
    @Transactional
    public void deletePricing(String id) {
        if (!coinPricingRepo.existsById(id)) {
            throw new AppException(ErrorCode.COIN_PRICING_NOT_FOUND);
        }

        coinPricingRepo.deleteById(id);
        log.info("Deleted coin pricing: {}", id);
    }

    private CoinPricingResponseDto toResponseDto(CoinPricing pricing) {
        return CoinPricingResponseDto.builder()
                .id(pricing.getId())
                .resourceType(pricing.getResourceType())
                .resourceTypeDisplayName(pricing.getResourceType().getDisplayName())
                .modelName(pricing.getModelName())
                .baseCost(pricing.getBaseCost())
                .unitType(pricing.getUnitType())
                .unitTypeDisplayName(
                        Objects.nonNull(pricing.getUnitType()) ? pricing.getUnitType().getDisplayName() : null)
                .unitMultiplier(pricing.getUnitMultiplier())
                .description(pricing.getDescription())
                .isActive(pricing.getIsActive())
                .createdAt(pricing.getCreatedAt())
                .updatedAt(pricing.getUpdatedAt())
                .build();
    }
}
