package com.datn.datnbe.payment.adapter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.datn.datnbe.payment.dto.request.PayosCreatePaymentRequest;
import com.datn.datnbe.payment.dto.request.PayosItemRequest;
import com.datn.datnbe.payment.dto.response.CheckoutResponse;
import com.datn.datnbe.payment.dto.response.PayosPaymentLinkResponse;
import com.datn.datnbe.payment.util.PayosSignatureUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * PayOS Payment Gateway Adapter
 * Implements PayOS API specification for payment processing
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PayosAdapter implements PaymentGatewayAdapter {

    @Value("${payos.client-id}")
    private String clientId;

    @Value("${payos.api-key}")
    private String apiKey;

    @Value("${payos.api-base-url:https://api-merchant.payos.vn}")
    private String apiBaseUrl;

    private final PayosSignatureUtil signatureUtil;
    private final RestTemplate restTemplate = new RestTemplate();

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

            // Convert amount to integer (VND doesn't use decimals)
            Integer amountInt = amount.intValue();

            // Generate signature
            String signature = signatureUtil
                    .generatePaymentLinkSignature(amountInt, cancelUrl, description, orderCode, successUrl);

            // Create item list (at least one item required by PayOS)
            List<PayosItemRequest> items = new ArrayList<>();
            items.add(PayosItemRequest.builder()
                    .name(description != null && !description.isEmpty() ? description : "Payment")
                    .quantity(1)
                    .price(amountInt)
                    .build());

            // Build request
            PayosCreatePaymentRequest request = PayosCreatePaymentRequest.builder()
                    .orderCode(orderCode)
                    .amount(amountInt)
                    .description(description)
                    .buyerName(null) // Can be extended to accept buyer info
                    .buyerEmail(null)
                    .buyerPhone(null)
                    .items(items)
                    .cancelUrl(cancelUrl)
                    .returnUrl(successUrl)
                    .signature(signature)
                    .build();

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-client-id", clientId);
            headers.set("x-api-key", apiKey);

            HttpEntity<PayosCreatePaymentRequest> entity = new HttpEntity<>(request, headers);

            // Call PayOS API
            String url = apiBaseUrl + "/v2/payment-requests";
            log.info("Creating PayOS payment link for order: {} with amount: {}", orderCode, amount);
            log.debug("PayOS API URL: {}", url);

            ResponseEntity<PayosPaymentLinkResponse> response = restTemplate
                    .exchange(url, HttpMethod.POST, entity, PayosPaymentLinkResponse.class);

            PayosPaymentLinkResponse payosResponse = response.getBody();

            if (payosResponse == null || payosResponse.getData() == null) {
                throw new RuntimeException("PayOS returned empty response");
            }

            // Check response code
            if (!"00".equals(payosResponse.getCode())) {
                throw new RuntimeException("PayOS returned error: " + payosResponse.getDesc());
            }

            String checkoutUrl = payosResponse.getData().getCheckoutUrl();
            log.info("Successfully created PayOS payment link: {}", payosResponse.getData().getPaymentLinkId());
            
            return checkoutUrl;

        } catch (Exception e) {
            log.error("Error creating PayOS payment link for order: {}", orderInvoiceNumber, e);
            throw new RuntimeException("Failed to create PayOS payment link: " + e.getMessage(), e);
        }
    }

    @Override
    public PayosPaymentLinkResponse getOrderDetails(String orderCode) {
        try {
            String url = apiBaseUrl + "/v2/payment-requests/" + orderCode;

            HttpHeaders headers = new HttpHeaders();
            headers.set("x-client-id", clientId);
            headers.set("x-api-key", apiKey);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<PayosPaymentLinkResponse> response = restTemplate
                    .exchange(url, HttpMethod.GET, entity, PayosPaymentLinkResponse.class);

            log.info("Retrieved PayOS payment link info for: {}", orderCode);
            return response.getBody();

        } catch (Exception e) {
            log.error("Error getting PayOS payment link info for: {}", orderCode, e);
            throw new RuntimeException("Failed to get PayOS payment link info: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean cancelOrder(String orderCode) {
        try {
            String url = apiBaseUrl + "/v2/payment-requests/" + orderCode + "/cancel";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-client-id", clientId);
            headers.set("x-api-key", apiKey);

            // PayOS cancel endpoint may require empty body or cancellation reason
            HttpEntity<String> entity = new HttpEntity<>("{}", headers);

            ResponseEntity<PayosPaymentLinkResponse> response = restTemplate
                    .exchange(url, HttpMethod.POST, entity, PayosPaymentLinkResponse.class);

            log.info("Cancelled PayOS payment link: {}", orderCode);
            return response.getStatusCode().is2xxSuccessful();

        } catch (Exception e) {
            log.error("Error cancelling PayOS payment link: {}", orderCode, e);
            return false;
        }
    }
}
