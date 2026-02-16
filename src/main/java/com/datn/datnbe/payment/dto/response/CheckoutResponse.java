package com.datn.datnbe.payment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutResponse {

    private String transactionId;

    private String orderInvoiceNumber;

    private String gate;

    private String checkoutUrl;

    private String referenceCode;

    private java.math.BigDecimal amount;

    private String status;
}
