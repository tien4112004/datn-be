package com.datn.datnbe.payment.adapter;

import java.math.BigDecimal;

public interface PaymentGatewayAdapter {

    String createCheckout(String orderInvoiceNumber,
            BigDecimal amount,
            String description,
            String customerId,
            String successUrl,
            String errorUrl,
            String cancelUrl,
            String paymentMethod);

    Object getOrderDetails(String orderId);

    boolean cancelOrder(String orderInvoiceNumber);

    String getGatewayName();

    default boolean supports(String gatewayName) {
        return getGatewayName().equalsIgnoreCase(gatewayName);
    }
}
