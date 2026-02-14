package com.datn.datnbe.payment.apiclient;

import com.datn.datnbe.payment.dto.response.CheckoutResponse;
import com.datn.datnbe.payment.dto.response.SepayOrderDetailResponse;
import com.datn.datnbe.payment.util.SepaySignatureUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Sepay Payment Gateway Client
 * Implements Sepay API specification for payment processing
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentGatewayClient {

    @Value("${sepay.merchant-id}")
    private String merchantId;

    @Value("${sepay.api-key}")
    private String apiKey;

    @Value("${sepay.secret-key}")
    private String secretKey;

    @Value("${sepay.checkout-base-url:https://pay-sandbox.sepay.vn}")
    private String checkoutBaseUrl;

    @Value("${sepay.api-base-url:https://pgapi-sandbox.sepay.vn}")
    private String apiBaseUrl;

    private final SepaySignatureUtil signatureUtil;
    private final RestTemplate restTemplate = new RestTemplate();

    public CheckoutResponse createCheckout(String orderInvoiceNumber,
            BigDecimal amount,
            String description,
            String customerId,
            String successUrl,
            String errorUrl,
            String cancelUrl,
            String paymentMethod) {

        try {
            // Build form fields in exact order required by Sepay
            Map<String, String> formFields = signatureUtil.createOrderedFieldsMap();
            formFields.put("merchant", merchantId);
            formFields.put("operation", "PURCHASE");

            // payment_method is optional - if not provided, user can choose on Sepay page
            if (paymentMethod != null && !paymentMethod.isEmpty()) {
                formFields.put("payment_method", paymentMethod);
            }

            formFields.put("order_amount", amount.toString());
            formFields.put("currency", "VND");
            formFields.put("order_invoice_number", orderInvoiceNumber);
            formFields.put("order_description", description);

            if (customerId != null && !customerId.isEmpty()) {
                formFields.put("customer_id", customerId);
            }

            formFields.put("success_url", successUrl);
            formFields.put("error_url", errorUrl);
            formFields.put("cancel_url", cancelUrl);

            // Generate signature
            String signature = signatureUtil.generateCheckoutSignature(formFields);
            formFields.put("signature", signature);

            // Checkout base URL is configurable (set to sandbox or production via properties)
            String checkoutUrl = checkoutBaseUrl + "/v1/checkout/init";

            log.info("Created checkout form for order: {} with amount: {}", orderInvoiceNumber, amount);
            log.debug("Checkout URL: {}", checkoutUrl);

            return CheckoutResponse.builder()
                    .orderInvoiceNumber(orderInvoiceNumber)
                    .gate("SEPAY")
                    .checkoutUrl(checkoutUrl)
                    .formFields(formFields)
                    .amount(amount)
                    .status("PENDING")
                    .build();

        } catch (Exception e) {
            log.error("Error creating checkout for order: {}", orderInvoiceNumber, e);
            throw new RuntimeException("Failed to create checkout: " + e.getMessage(), e);
        }
    }

    public SepayOrderDetailResponse getOrderDetails(String orderId) {
        try {
            String url = apiBaseUrl + "/v1/order/detail/" + orderId;

            HttpHeaders headers = new HttpHeaders();
            // Sepay expects API token in Bearer format (Authorization: Bearer <API_TOKEN>)
            headers.set("Authorization", "Bearer " + apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<SepayOrderDetailResponse> response = restTemplate
                    .exchange(url, HttpMethod.GET, entity, SepayOrderDetailResponse.class);

            log.info("Retrieved order details for: {}", orderId);
            return response.getBody();

        } catch (Exception e) {
            log.error("Error getting order details for: {}", orderId, e);
            throw new RuntimeException("Failed to get order details: " + e.getMessage(), e);
        }
    }

    public boolean cancelOrder(String orderInvoiceNumber) {
        try {
            String url = apiBaseUrl + "/v1/order/cancel";

            HttpHeaders headers = new HttpHeaders();
            // Use Bearer token as required by Sepay API
            headers.set("Authorization", "Bearer " + apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> requestBody = Map.of("order_invoice_number", orderInvoiceNumber);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map<String, Object>> response = restTemplate
                    .exchange(url, HttpMethod.POST, entity, new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            log.info("Cancelled order: {}", orderInvoiceNumber);
            return response.getStatusCode() == HttpStatus.OK;

        } catch (Exception e) {
            log.error("Error cancelling order: {}", orderInvoiceNumber, e);
            return false;
        }
    }

    public boolean voidTransaction(String orderInvoiceNumber) {
        try {
            String url = apiBaseUrl + "/v1/order/voidTransaction";

            HttpHeaders headers = new HttpHeaders();
            // Use Bearer token as required by Sepay API
            headers.set("Authorization", "Bearer " + apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> requestBody = Map.of("order_invoice_number", orderInvoiceNumber);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map<String, Object>> response = restTemplate
                    .exchange(url, HttpMethod.POST, entity, new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            log.info("Voided transaction: {}", orderInvoiceNumber);
            return response.getStatusCode() == HttpStatus.OK;

        } catch (Exception e) {
            log.error("Error voiding transaction: {}", orderInvoiceNumber, e);
            return false;
        }
    }


}
