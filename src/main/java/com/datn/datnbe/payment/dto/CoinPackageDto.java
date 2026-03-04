package com.datn.datnbe.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoinPackageDto {
    private String id;
    private String name;
    private Long price;
    private Long coin;
    private Long bonus;
    private Boolean isActive;
}
