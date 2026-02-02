package com.datn.datnbe.ai.dto.request;

import com.datn.datnbe.ai.enums.ResourceType;
import com.datn.datnbe.ai.enums.UnitType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CoinPricingCreateRequest {

    @NotNull(message = "Resource type is required")
    ResourceType resourceType;

    Integer modelId;

    @NotNull(message = "Base cost is required")
    @Min(value = 0, message = "Base cost must be non-negative")
    Integer baseCost;

    UnitType unitType = UnitType.PER_REQUEST;

    String description;
}
