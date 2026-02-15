package com.datn.datnbe.payment.entity;

import java.math.BigDecimal;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "payment_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String description;

    private String referenceCode;

    @Column(unique = true)
    private String orderInvoiceNumber; // Sepay order invoice number (e.g., DH1234567890)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    private String sepayTransactionId;

    private String payosPaymentLinkId; // PayOS payment link ID

    @Column(columnDefinition = "TEXT")
    private String transactionData;

    @Column(nullable = false, updatable = false)
    private Date createdAt;

    @Column(nullable = false)
    private Date updatedAt;

    private Date completedAt;

    // refund metadata (nullable)
    private String refundId;
    private Date refundedAt;
    @Column(nullable = false)
    @Builder.Default
    private String gate = "SEPAY";

    public enum TransactionStatus {
        PENDING, COMPLETED, FAILED, CANCELLED, PROCESSING, REFUNDED
    }
}
