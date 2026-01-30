package com.datn.datnbe.ai.management;

import com.datn.datnbe.ai.api.CoinPricingApi;
import com.datn.datnbe.ai.dto.request.CoinPricingCreateRequest;
import com.datn.datnbe.ai.dto.request.CoinPricingUpdateRequest;
import com.datn.datnbe.ai.dto.response.CoinPricingResponseDto;
import com.datn.datnbe.ai.entity.CoinPricing;
import com.datn.datnbe.ai.entity.ModelConfigurationEntity;
import com.datn.datnbe.ai.enums.ResourceType;
import com.datn.datnbe.ai.repository.CoinPricingRepo;
import com.datn.datnbe.ai.repository.interfaces.ModelConfigurationRepo;
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
    ModelConfigurationRepo modelConfigurationRepo;

    @Override
    public List<CoinPricingResponseDto> getAllPricing(ResourceType resourceType) {
        List<CoinPricing> pricingList;

        if (resourceType != null) {
            pricingList = coinPricingRepo.findByResourceType(resourceType);
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
        // Check for duplicate resource_type + model_id combination
        boolean exists;
        if (request.getModelId() == null) {
            exists = coinPricingRepo.existsByResourceTypeAndModelIsNull(request.getResourceType());
        } else {
            exists = coinPricingRepo.existsByResourceTypeAndModelModelId(request.getResourceType(),
                    request.getModelId());
        }
        if (exists) {
            throw new AppException(ErrorCode.COIN_PRICING_ALREADY_EXISTS);
        }

        // Look up the model if modelId is provided
        ModelConfigurationEntity model = null;
        if (request.getModelId() != null) {
            model = modelConfigurationRepo.getModelById(request.getModelId());
            if (model == null) {
                throw new AppException(ErrorCode.MODEL_NOT_FOUND);
            }
        }

        CoinPricing pricing = CoinPricing.builder()
                .resourceType(request.getResourceType())
                .model(model)
                .baseCost(request.getBaseCost())
                .unitType(request.getUnitType())
                .description(request.getDescription())
                .build();

        CoinPricing saved = coinPricingRepo.save(pricing);
        log.info("Created coin pricing: {} for resource type: {}, model_id: {}",
                saved.getId(),
                saved.getResourceType(),
                request.getModelId());

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
        if (request.getDescription() != null) {
            pricing.setDescription(request.getDescription());
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
        ModelConfigurationEntity model = pricing.getModel();
        return CoinPricingResponseDto.builder()
                .id(pricing.getId())
                .resourceType(pricing.getResourceType())
                .resourceTypeDisplayName(pricing.getResourceType().getDisplayName())
                .modelId(model != null ? model.getModelId() : null)
                .modelName(model != null ? model.getModelName() : null)
                .modelDisplayName(model != null ? model.getDisplayName() : null)
                .baseCost(pricing.getBaseCost())
                .unitType(pricing.getUnitType())
                .unitTypeDisplayName(
                        Objects.nonNull(pricing.getUnitType()) ? pricing.getUnitType().getDisplayName() : null)
                .description(pricing.getDescription())
                .createdAt(pricing.getCreatedAt())
                .updatedAt(pricing.getUpdatedAt())
                .build();
    }
}
