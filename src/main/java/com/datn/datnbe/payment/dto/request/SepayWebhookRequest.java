package com.datn.datnbe.payment.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Sepay Webhook/IPN Request
 * Matches the payload structure observed from Sepay (contains nested `order`, `transaction`, `customer` objects)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SepayWebhookRequest {
    // Top-level fields
    Long timestamp;

    @JsonProperty("notification_type")
    String notificationType;

    Order order;
    Transaction transaction;
    Customer customer;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Order {
        String id;

        @JsonProperty("order_id")
        String orderId;

        @JsonProperty("order_status")
        String orderStatus;

        @JsonProperty("order_currency")
        String orderCurrency;

        // Sepay sometimes sends amount as string like "100000.00"
        @JsonProperty("order_amount")
        String orderAmount;

        @JsonProperty("order_invoice_number")
        String orderInvoiceNumber;

        @JsonProperty("order_description")
        String orderDescription;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Transaction {
        String id;

        @JsonProperty("payment_method")
        String paymentMethod;

        @JsonProperty("transaction_id")
        String transactionId;

        @JsonProperty("transaction_status")
        String transactionStatus;

        @JsonProperty("transaction_amount")
        String transactionAmount;

        @JsonProperty("authentication_status")
        String authenticationStatus;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Customer {
        String id;

        @JsonProperty("customer_id")
        String customerId;
    }
}
