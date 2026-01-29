package com.datn.datnbe.ai.dto.response;

import com.datn.datnbe.ai.enums.ResourceType;
import com.datn.datnbe.ai.enums.UnitType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CoinPricingResponseDto {
    String id;
    ResourceType resourceType;
    String resourceTypeDisplayName;
    String modelName;
    Integer baseCost;
    UnitType unitType;
    String unitTypeDisplayName;
    BigDecimal unitMultiplier;
    String description;
    Boolean isActive;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
