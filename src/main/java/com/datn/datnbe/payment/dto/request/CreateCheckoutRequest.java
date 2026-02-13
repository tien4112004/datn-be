package com.datn.datnbe.payment.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCheckoutRequest {
    private java.math.BigDecimal amount;
    private String description;
    private String referenceCode;
    private String gate;
    private String successUrl;
    private String errorUrl;
    private String cancelUrl;
}
