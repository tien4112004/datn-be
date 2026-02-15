package com.datn.datnbe.payment.util;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * PayOS signature utility
 * Handles signature generation and validation for PayOS API
 * Uses HMAC-SHA256 algorithm as per PayOS specification
 */
@Slf4j
@Component
public class PayosSignatureUtil {

    private static final String HMAC_SHA256 = "HmacSHA256";

    @Value("${payos.checksum-key}")
    private String checksumKey;

    /**
     * Generate signature for payment link creation
     * Format:
     * amount=$amount&cancelUrl=$cancelUrl&description=$description&orderCode=$orderCode&returnUrl=$returnUrl
     * Fields must be sorted alphabetically
     *
     * @param amount      Payment amount
     * @param cancelUrl   Cancel URL
     * @param description Payment description
     * @param orderCode   Order code
     * @param returnUrl   Return URL
     * @return HMAC-SHA256 signature as hex string
     */
    public String generatePaymentLinkSignature(Integer amount,
            String cancelUrl,
            String description,
            Long orderCode,
            String returnUrl) {

        // Create ordered map (sorted alphabetically by key)
        Map<String, String> data = new LinkedHashMap<>();
        data.put("amount", String.valueOf(amount));
        data.put("cancelUrl", cancelUrl != null ? cancelUrl : "");
        data.put("description", description != null ? description : "");
        data.put("orderCode", String.valueOf(orderCode));
        data.put("returnUrl", returnUrl != null ? returnUrl : "");

        // Build data string in format: key1=value1&key2=value2&...
        String dataString = data.entrySet()
                .stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));

        log.debug("PayOS signature data string: {}", dataString);

        return generateHmacSha256(dataString);
    }

    /**
     * Validate webhook signature
     *
     * @param webhookData       The webhook data object
     * @param receivedSignature The signature received from PayOS
     * @return true if signature is valid, false otherwise
     */
    public boolean validateWebhookSignature(Object webhookData, String receivedSignature) {
        try {
            // For webhook validation, we need to serialize the data object
            // and compute HMAC-SHA256 of the serialized data
            // The exact format depends on PayOS webhook specification

            // According to PayOS docs, webhook signature is computed from the entire data
            // object
            // We'll use a simple approach: convert data to string and compute signature
            String dataString = convertWebhookDataToString(webhookData);
            String computedSignature = generateHmacSha256(dataString);

            boolean isValid = computedSignature.equalsIgnoreCase(receivedSignature);

            if (!isValid) {
                log.warn("PayOS webhook signature validation failed. Expected: {}, Received: {}",
                        computedSignature,
                        receivedSignature);
            }

            return isValid;

        } catch (Exception e) {
            log.error("Error validating PayOS webhook signature", e);
            return false;
        }
    }

    /**
     * Generate HMAC-SHA256 signature
     *
     * @param data Data to sign
     * @return Hex-encoded signature
     */
    private String generateHmacSha256(String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(checksumKey.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(secretKeySpec);

            byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hmacBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Error generating HMAC-SHA256 signature", e);
            throw new RuntimeException("Failed to generate signature", e);
        }
    }

    /**
     * Convert webhook data to string for signature computation
     * This is a simplified implementation - may need adjustment based on actual
     * PayOS webhook format
     *
     * @param webhookData Webhook data object
     * @return String representation for signature computation
     */
    private String convertWebhookDataToString(Object webhookData) {
        // Use JSON serialization for a deterministic representation instead of
        // relying on the object's toString(). This will make the computed HMAC
        // match webhook bodies that are JSON-serialized by PayOS.
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String json = webhookData == null ? "" : mapper.writeValueAsString(webhookData);
            log.debug("PayOS webhook signature string: {}", json);
            return json;
        } catch (Exception e) {
            log.warn("Failed to serialize webhook data for signature computation, falling back to toString()", e);
            return webhookData != null ? webhookData.toString() : "";
        }
    }
}
