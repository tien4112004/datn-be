package com.datn.datnbe.payment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Sepay Order Detail Response
 * Matches the structure returned by Sepay API /v1/order/detail/{order_id}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SepayOrderDetailResponse {

    private OrderData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderData {
        private String id;
        private String customerId;
        private String orderId; // Sepay order ID (e.g., SEPAY-68BA83CE637C1)
        private String orderInvoiceNumber; // Your order invoice number
        private String orderStatus; // CAPTURED, CANCELLED, AUTHENTICATION_NOT_NEEDED
        private BigDecimal orderAmount;
        private String orderCurrency;
        private String orderDescription;
        private String authenticationStatus;
        private String createdAt;
        private String updatedAt;
        private List<Transaction> transactions;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Transaction {
        private String id;
        private String paymentMethod; // CARD, BANK_TRANSFER, NAPAS_BANK_TRANSFER
        private String transactionType; // PAYMENT
        private String transactionAmount;
        private String transactionCurrency;
        private String transactionStatus; // APPROVED, DECLINED
        private String authenticationStatus;
        private String cardNumber;
        private String cardHolderName;
        private String cardExpiry;
        private String cardFundingMethod; // DEBIT, CREDIT
        private String cardBrand; // MASTERCARD, VISA, etc.
        private String transactionDate;
        private String transactionLastUpdatedDate;
    }
}
