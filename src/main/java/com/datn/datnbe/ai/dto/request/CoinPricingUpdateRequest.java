package com.datn.datnbe.ai.dto.request;

import com.datn.datnbe.ai.enums.UnitType;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CoinPricingUpdateRequest {

    @Min(value = 0, message = "Base cost must be non-negative")
    Integer baseCost;

    UnitType unitType;

    String description;
}
