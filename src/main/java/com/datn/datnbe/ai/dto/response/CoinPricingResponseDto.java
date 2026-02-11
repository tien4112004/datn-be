package com.datn.datnbe.ai.dto.response;

import com.datn.datnbe.ai.enums.ResourceType;
import com.datn.datnbe.ai.enums.UnitType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CoinPricingResponseDto {
    String id;
    ResourceType resourceType;
    String resourceTypeDisplayName;
    Integer modelId;
    String modelName;
    String modelDisplayName;
    Integer baseCost;
    UnitType unitType;
    String unitTypeDisplayName;
    String description;
    Date createdAt;
    Date updatedAt;
}
