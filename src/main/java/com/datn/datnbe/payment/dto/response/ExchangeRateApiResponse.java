package com.datn.datnbe.payment.dto.response;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExchangeRateApiResponse {

    private String result;

    @JsonProperty("base_code")
    private String baseCode;

    @JsonProperty("conversion_amounts")
    private Map<String, Double> conversionAmounts;
}
