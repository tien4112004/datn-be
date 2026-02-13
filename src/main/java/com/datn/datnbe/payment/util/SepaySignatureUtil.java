package com.datn.datnbe.payment.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

/**
 * Sepay Signature Utility
 * Implements HMAC-SHA256 signature generation according to Sepay API
 * specification
 */
@Slf4j
@Component
public class SepaySignatureUtil {

    @Value("${sepay.secret-key}")
    private String secretKey;

    @PostConstruct
    public void init() {
        if (secretKey == null || secretKey.isBlank()) {
            log.error(
                    "Sepay secret key is not configured. Please set 'sepay.secret-key' property or SEPAY_SECRET environment variable.");
            throw new IllegalStateException(
                    "Sepay secret key is not configured. Please set 'sepay.secret-key' property or SEPAY_SECRET environment variable.");
        }
    }

    /**
     * Generate signature for Sepay checkout form
     * According to Sepay docs, signature is created from specific fields in exact
     * order:
     * merchant, operation, payment_method, order_amount, currency,
     * order_invoice_number, order_description, customer_id,
     * success_url, error_url, cancel_url
     *
     * @param fields Map of form fields
     * @return Base64 encoded HMAC-SHA256 signature
     */
    public String generateCheckoutSignature(Map<String, String> fields) {
        try {
            // Define the exact order of fields to sign (as per Sepay documentation)
            String[] signedFieldNames = {"merchant", "operation", "payment_method", "order_amount", "currency",
                    "order_invoice_number", "order_description", "customer_id", "success_url", "error_url",
                    "cancel_url"};

            // Build the signature string: field1=value1,field2=value2,...
            StringBuilder signedString = new StringBuilder();
            boolean first = true;

            for (String fieldName : signedFieldNames) {
                if (fields.containsKey(fieldName) && fields.get(fieldName) != null) {
                    if (!first) {
                        signedString.append(",");
                    }
                    signedString.append(fieldName).append("=").append(fields.get(fieldName));
                    first = false;
                }
            }

            log.debug("Signature string: {}", signedString.toString());

            // Generate HMAC-SHA256 signature
            return generateHmacSha256(signedString.toString(), secretKey);

        } catch (Exception e) {
            log.error("Error generating checkout signature", e);
            throw new RuntimeException("Failed to generate signature", e);
        }
    }

    /**
     * Verify webhook signature from Sepay
     *
     * @param data              Webhook data as string
     * @param receivedSignature Signature from webhook
     * @return true if signature is valid
     */
    public boolean verifyWebhookSignature(String data, String receivedSignature) {
        try {
            String computedSignature = generateHmacSha256(data, secretKey);
            return constantTimeEquals(computedSignature, receivedSignature);
        } catch (Exception e) {
            log.error("Error verifying webhook signature", e);
            return false;
        }
    }

    /**
     * Generate HMAC-SHA256 signature
     *
     * @param data Data to sign
     * @param key  Secret key
     * @return Base64 encoded signature
     */
    private String generateHmacSha256(String data, String key) throws Exception {
        if (key == null || key.isEmpty()) {
            log.error("Attempted to generate HMAC with empty Sepay secret key");
            throw new IllegalArgumentException(
                    "Sepay secret key is empty. Set 'sepay.secret-key' or SEPAY_SECRET env var.");
        }
        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256Hmac.init(secretKeySpec);
        byte[] hash = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Constant-time string comparison to prevent timing attacks
     */
    private boolean constantTimeEquals(String a, String b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }

        int result = 0;
        byte[] aBytes = a.getBytes(StandardCharsets.UTF_8);
        byte[] bBytes = b.getBytes(StandardCharsets.UTF_8);

        int minLength = Math.min(aBytes.length, bBytes.length);
        for (int i = 0; i < minLength; i++) {
            result |= aBytes[i] ^ bBytes[i];
        }
        result |= aBytes.length ^ bBytes.length;
        return result == 0;
    }

    /**
     * Create ordered map for signature generation
     * Maintains insertion order which is critical for signature
     */
    public Map<String, String> createOrderedFieldsMap() {
        return new LinkedHashMap<>();
    }
}
