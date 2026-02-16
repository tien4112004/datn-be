package com.datn.datnbe.payment.adapter;

import java.math.BigDecimal;

import com.datn.datnbe.payment.dto.response.CheckoutResponse;

/**
 * Payment Gateway Adapter Interface
 *
 * Defines the contract for all payment gateway adapters.
 * Each payment gateway (SePay, PayOS, etc.) should implement this interface
 * to provide a consistent API for payment operations.
 */
public interface PaymentGatewayAdapter {

    /**
     * Create a checkout/payment link
     *
     * @param orderInvoiceNumber Unique order invoice number
     * @param amount             Payment amount
     * @param description        Payment description
     * @param customerId         Customer/user ID
     * @param successUrl         Success callback URL
     * @param errorUrl           Error callback URL
     * @param cancelUrl          Cancel callback URL
     * @param paymentMethod      Optional payment method (can be null)
     * @return CheckoutResponse containing checkout URL and details
     */
    CheckoutResponse createCheckout(String orderInvoiceNumber,
            BigDecimal amount,
            String description,
            String customerId,
            String successUrl,
            String errorUrl,
            String cancelUrl,
            String paymentMethod);

    /**
     * Get order/payment details
     *
     * @param orderId Order ID or invoice number
     * @return Order details (gateway-specific response)
     */
    Object getOrderDetails(String orderId);

    /**
     * Cancel an order/payment
     *
     * @param orderInvoiceNumber Order invoice number
     * @return true if cancellation successful, false otherwise
     */
    boolean cancelOrder(String orderInvoiceNumber);

    /**
     * Get the gateway name/identifier
     *
     * @return Gateway name (e.g., "SEPAY", "PAYOS")
     */
    String getGatewayName();

    /**
     * Check if this adapter supports the given gateway name
     *
     * @param gatewayName Gateway name to check
     * @return true if this adapter supports the gateway, false otherwise
     */
    default boolean supports(String gatewayName) {
        return getGatewayName().equalsIgnoreCase(gatewayName);
    }
}
