package com.datn.datnbe.payment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PayOS payment link response DTO
 * Matches PayOS API response for payment link creation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayosPaymentLinkResponse {

    /**
     * Response code
     * "00" = success
     */
    private String code;

    /**
     * Response description
     */
    private String desc;

    /**
     * Payment link data
     */
    private PaymentLinkData data;

    /**
     * Response signature for verification
     */
    private String signature;

    /**
     * Payment link data
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentLinkData {

        /**
         * Bank BIN
         */
        private String bin;

        /**
         * Account number
         */
        private String accountNumber;

        /**
         * Account name
         */
        private String accountName;

        /**
         * Payment amount
         */
        private Integer amount;

        /**
         * Payment description
         */
        private String description;

        /**
         * Order code
         */
        private Long orderCode;

        /**
         * Currency (VND)
         */
        private String currency;

        /**
         * Payment link ID
         */
        private String paymentLinkId;

        /**
         * Payment status
         * PENDING, PAID, CANCELLED, EXPIRED
         */
        private String status;

        /**
         * Checkout URL
         */
        private String checkoutUrl;

        /**
         * QR code string
         */
        private String qrCode;
    }
}
