package com.datn.datnbe.payment.service;

import java.util.Date;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.datn.datnbe.payment.dto.request.SepayWebhookRequest;
import com.datn.datnbe.payment.entity.PaymentTransaction;
import com.datn.datnbe.payment.entity.PaymentTransaction.TransactionStatus;
import com.datn.datnbe.payment.repository.PaymentTransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Extracted handler for Sepay webhook processing so PaymentService stays small.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SepayWebhookService {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final UserCoinService userCoinService;

    @Transactional
    public void handle(SepayWebhookRequest webhookRequest) {
        String orderInvoice = null;
        String orderStatus = null;
        java.math.BigDecimal orderAmount = null;
        String orderId = null;

        if (webhookRequest != null && webhookRequest.getOrder() != null) {
            var order = webhookRequest.getOrder();
            orderInvoice = order.getOrderInvoiceNumber();
            orderStatus = order.getOrderStatus();
            orderId = order.getOrderId() != null ? order.getOrderId() : order.getId();
            try {
                if (order.getOrderAmount() != null && !order.getOrderAmount().isBlank()) {
                    orderAmount = new java.math.BigDecimal(order.getOrderAmount());
                }
            } catch (Exception e) {
                log.warn("Unable to parse order amount: {}", order.getOrderAmount());
            }
        }

        log.info("Handling Sepay webhook for order invoice: {} status: {}", orderInvoice, orderStatus);

        if (orderInvoice == null) {
            log.warn("Webhook received without order_invoice_number: {}", webhookRequest);
            return;
        }

        var optional = paymentTransactionRepository.findByOrderInvoiceNumber(orderInvoice);
        if (optional.isEmpty()) {
            log.warn("Transaction not found for order invoice: {}. Ignoring webhook.", orderInvoice);
            return;
        }

        PaymentTransaction tx = optional.get();

        try {
            if ("CAPTURED".equalsIgnoreCase(orderStatus)) {
                if (tx.getStatus() != TransactionStatus.COMPLETED) {
                    tx.setStatus(TransactionStatus.COMPLETED);
                    tx.setCompletedAt(new Date());

                    long coins = 0L;
                    if (orderAmount != null)
                        coins = orderAmount.longValue() / 1000L;
                    userCoinService.addCoin(tx.getUserId(), coins, "sepay");

                    log.info("Payment completed (webhook): {} - added {} coins to user {}",
                            tx.getId(),
                            coins,
                            tx.getUserId());
                } else {
                    log.info("Webhook CAPTURED but tx already COMPLETED: {}", tx.getId());
                }
            } else if ("CANCELLED".equalsIgnoreCase(orderStatus)) {
                tx.setStatus(TransactionStatus.CANCELLED);
                log.info("Payment cancelled (webhook): {}", tx.getId());
            } else if ("AUTHENTICATION_NOT_NEEDED".equalsIgnoreCase(orderStatus)) {
                tx.setStatus(TransactionStatus.PENDING);
                log.info("Payment pending (webhook): {}", tx.getId());
            } else {
                tx.setStatus(TransactionStatus.FAILED);
                log.info("Payment failed (webhook) status={}: {}", orderStatus, tx.getId());
            }

            // prefer Sepay transaction id from nested `transaction` object, fall back to orderId
            String sepayTxId = null;
            if (webhookRequest != null && webhookRequest.getTransaction() != null
                    && webhookRequest.getTransaction().getTransactionId() != null
                    && !webhookRequest.getTransaction().getTransactionId().isBlank()) {
                sepayTxId = webhookRequest.getTransaction().getTransactionId();
            } else if (orderId != null && !orderId.isBlank()) {
                sepayTxId = orderId;
            }
            if (sepayTxId != null) {
                tx.setSepayTransactionId(sepayTxId);
            }

            tx.setUpdatedAt(new Date());
            paymentTransactionRepository.save(tx);

        } catch (Exception e) {
            log.error("Error processing Sepay webhook for orderInvoice={}: {}", orderInvoice, e.getMessage(), e);
            // don't rethrow - webhook endpoint should return 200 to avoid retries from Sepay
        }
    }
}
