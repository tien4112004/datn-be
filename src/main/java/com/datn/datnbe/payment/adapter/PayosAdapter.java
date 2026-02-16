package com.datn.datnbe.payment.adapter;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import vn.payos.PayOS;
import vn.payos.exception.PayOSException;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;
import vn.payos.model.v2.paymentRequests.PaymentLink;

import lombok.extern.slf4j.Slf4j;

/**
 * PayOS Payment Gateway Adapter
 * Implements PayOS API specification for payment processing using official PayOS SDK
 */
@Slf4j
@Component
public class PayosAdapter implements PaymentGatewayAdapter {

    @Value("${payos.client-id}")
    private String clientId;

    @Value("${payos.api-key}")
    private String apiKey;

    @Value("${payos.checksum-key}")
    private String checksumKey;

    private PayOS payOSClient;

    private synchronized PayOS getPayOSClient() {
        if (payOSClient == null) {
            payOSClient = new PayOS(clientId, apiKey, checksumKey);
        }
        return payOSClient;
    }

    @Override
    public String getGatewayName() {
        return "PAYOS";
    }

    @Override
    public String createCheckout(String orderInvoiceNumber,
            BigDecimal amount,
            String description,
            String customerId,
            String successUrl,
            String errorUrl,
            String cancelUrl,
            String paymentMethod) {

        try {
            // Convert orderInvoiceNumber to Long for PayOS
            Long orderCode = Long.parseLong(orderInvoiceNumber);

            // Convert amount to long (VND doesn't use decimals)
            Long amountLong = amount.longValue();

            // Create item (at least one item required by PayOS)
            PaymentLinkItem item = PaymentLinkItem.builder()
                    .name(description != null && !description.isEmpty() ? description : "Payment")
                    .quantity(1)
                    .price(amountLong)
                    .build();

            // Build request using PayOS SDK
            CreatePaymentLinkRequest request = CreatePaymentLinkRequest.builder()
                    .orderCode(orderCode)
                    .amount(amountLong)
                    .description(description)
                    .buyerName(null) // Can be extended to accept buyer info
                    .buyerEmail(null)
                    .buyerPhone(null)
                    .item(item)
                    .cancelUrl(cancelUrl)
                    .returnUrl(successUrl)
                    .build();

            // Call PayOS API
            log.info("Creating PayOS payment link for order: {} with amount: {}", orderCode, amount);

            CreatePaymentLinkResponse response = getPayOSClient().paymentRequests().create(request);

            if (response == null || response.getCheckoutUrl() == null) {
                throw new RuntimeException("PayOS returned empty checkout URL");
            }

            String checkoutUrl = response.getCheckoutUrl();
            log.info("Successfully created PayOS payment link: {}", response.getPaymentLinkId());
            
            return checkoutUrl;

        } catch (PayOSException e) {
            log.error("Error creating PayOS payment link for order: {}", orderInvoiceNumber, e);
            throw new RuntimeException("Failed to create PayOS payment link: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error creating PayOS payment link for order: {}", orderInvoiceNumber, e);
            throw new RuntimeException("Failed to create PayOS payment link: " + e.getMessage(), e);
        }
    }

    @Override
    public PaymentLink getOrderDetails(String orderCode) {
        try {
            log.info("Retrieved PayOS payment link info for: {}", orderCode);
            
            // Try to get by payment link ID first
            try {
                return getPayOSClient().paymentRequests().get(orderCode);
            } catch (PayOSException e) {
                // If not found by payment link ID, try by order code (as Long)
                Long orderCodeLong = Long.parseLong(orderCode);
                return getPayOSClient().paymentRequests().get(orderCodeLong);
            }

        } catch (PayOSException e) {
            log.error("Error getting PayOS payment link info for: {}", orderCode, e);
            throw new RuntimeException("Failed to get PayOS payment link info: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error getting PayOS payment link info for: {}", orderCode, e);
            throw new RuntimeException("Failed to get PayOS payment link info: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean cancelOrder(String orderCode) {
        try {
            // Try to cancel by payment link ID first
            try {
                getPayOSClient().paymentRequests().cancel(orderCode);
                log.info("Cancelled PayOS payment link: {}", orderCode);
                return true;
            } catch (PayOSException e) {
                // If not found by payment link ID, try by order code (as Long)
                Long orderCodeLong = Long.parseLong(orderCode);
                getPayOSClient().paymentRequests().cancel(orderCodeLong);
                log.info("Cancelled PayOS payment link: {}", orderCode);
                return true;
            }

        } catch (PayOSException e) {
            log.error("Error cancelling PayOS payment link: {}", orderCode, e);
            return false;
        } catch (Exception e) {
            log.error("Error cancelling PayOS payment link: {}", orderCode, e);
            return false;
        }
    }
}
