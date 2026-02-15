package com.datn.datnbe.payment.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PayOS webhook request DTO
 * Matches PayOS webhook data structure
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayosWebhookRequest {

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
     * Success flag
     */
    private Boolean success;

    /**
     * Webhook data
     */
    private WebhookData data;

    /**
     * Webhook signature for verification
     */
    private String signature;

    /**
     * Webhook data
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WebhookData {

        /**
         * Order code
         */
        private Long orderCode;

        /**
         * Payment amount
         */
        private Integer amount;

        /**
         * Payment description
         */
        private String description;

        /**
         * Account number
         */
        private String accountNumber;

        /**
         * Transaction reference
         */
        private String reference;

        /**
         * Transaction date time
         */
        private String transactionDateTime;

        /**
         * Currency (VND)
         */
        private String currency;

        /**
         * Payment link ID
         */
        private String paymentLinkId;

        /**
         * Response code
         */
        private String code;

        /**
         * Response description
         */
        private String desc;

        /**
         * Counter account bank ID
         */
        private String counterAccountBankId;

        /**
         * Counter account bank name
         */
        private String counterAccountBankName;

        /**
         * Counter account name
         */
        private String counterAccountName;

        /**
         * Counter account number
         */
        private String counterAccountNumber;

        /**
         * Virtual account name
         */
        private String virtualAccountName;

        /**
         * Virtual account number
         */
        private String virtualAccountNumber;
    }
}
