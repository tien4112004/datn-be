package com.datn.datnbe.payment.dto.request;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PayOS create payment request DTO
 * Matches PayOS API specification for creating payment links
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayosCreatePaymentRequest {

    /**
     * Order code (merchant's order ID)
     * Must be unique
     */
    private Long orderCode;

    /**
     * Payment amount (in VND)
     */
    private Integer amount;

    /**
     * Payment description
     * Limited to 9 characters for non-linked bank accounts
     */
    private String description;

    /**
     * Buyer name (optional)
     * Used for electronic invoice integration
     */
    private String buyerName;

    /**
     * Buyer company name (optional)
     * Used for electronic invoice integration
     */
    private String buyerCompanyName;

    /**
     * Buyer tax code (optional)
     * Used for electronic invoice integration
     */
    private String buyerTaxCode;

    /**
     * Buyer address (optional)
     * Used for electronic invoice integration
     */
    private String buyerAddress;

    /**
     * Buyer email (optional)
     * Used for electronic invoice integration
     */
    private String buyerEmail;

    /**
     * Buyer phone (optional)
     * Used for electronic invoice integration
     */
    private String buyerPhone;

    /**
     * List of items in the payment
     */
    private List<PayosItemRequest> items;

    /**
     * Cancel URL - where to redirect when user cancels
     */
    private String cancelUrl;

    /**
     * Return URL - where to redirect on successful payment
     */
    private String returnUrl;

    /**
     * Payment link expiration time (Unix timestamp, Int32)
     * Optional
     */
    private Integer expiredAt;

    /**
     * Signature for data integrity verification
     * Generated using HMAC_SHA256 with checksum key
     * Format:
     * amount=$amount&cancelUrl=$cancelUrl&description=$description&orderCode=$orderCode&returnUrl=$returnUrl
     */
    private String signature;
}
