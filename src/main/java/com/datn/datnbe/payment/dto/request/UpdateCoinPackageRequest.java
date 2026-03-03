package com.datn.datnbe.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCoinPackageRequest {

    @NotBlank(message = "Package name is required")
    private String name;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than 0")
    private Long price;

    @NotNull(message = "Coin is required")
    @Positive(message = "Coin must be greater than 0")
    private Long coin;

    @NotNull(message = "Bonus coins is required")
    private Long bonus;

    private Boolean isActive;
}
