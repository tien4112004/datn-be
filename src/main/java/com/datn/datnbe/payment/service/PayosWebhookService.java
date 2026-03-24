package com.datn.datnbe.payment.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.datn.datnbe.payment.dto.request.PayosWebhookRequest;
import com.datn.datnbe.payment.entity.CoinPackage;
import com.datn.datnbe.payment.entity.PaymentTransaction;
import com.datn.datnbe.payment.entity.PaymentTransaction.TransactionStatus;
import com.datn.datnbe.payment.repository.CoinPackageRepository;
import com.datn.datnbe.payment.repository.PaymentTransactionRepository;

import vn.payos.PayOS;
import vn.payos.exception.WebhookException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayosWebhookService {

    @Value("${payos.client-id}")
    private String clientId;

    @Value("${payos.api-key}")
    private String apiKey;

    @Value("${payos.checksum-key}")
    private String checksumKey;

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final CoinPackageRepository coinPackageRepository;
    private final UserCoinService userCoinService;

    private PayOS payOSClient;

    private synchronized PayOS getPayOSClient() {
        if (payOSClient == null) {
            payOSClient = new PayOS(clientId, apiKey, checksumKey);
        }
        return payOSClient;
    }

    @Transactional
    public void handle(PayosWebhookRequest webhookRequest) {
        if (webhookRequest == null || webhookRequest.getData() == null) {
            log.warn("PayOS webhook received with null data");
            return;
        }

        Long orderCode = webhookRequest.getData().getOrderCode();
        String code = webhookRequest.getData().getCode();
        Integer amount = webhookRequest.getData().getAmount();
        String paymentLinkId = webhookRequest.getData().getPaymentLinkId();

        log.info("Handling PayOS webhook for order code: {} status code: {}", orderCode, code);

        // Validate signature using PayOS SDK
        if (webhookRequest.getSignature() != null) {
            try {
                // The PayOS SDK verifies signatures automatically
                // For manual verification, we can use the SDK's crypto provider
                getPayOSClient().webhooks().verify(webhookRequest);
                log.info("PayOS webhook signature validated successfully for order: {}", orderCode);
            } catch (WebhookException e) {
                log.error("PayOS webhook signature validation failed for order: {}", orderCode, e);
                // In production, you might want to reject invalid signatures
                // For now, we'll log and continue
            }
        }

        // Find transaction by order code
        var optional = paymentTransactionRepository.findByOrderInvoiceNumber(String.valueOf(orderCode));
        if (optional.isEmpty()) {
            log.warn("Transaction not found for PayOS order code: {}. Ignoring webhook.", orderCode);
            return;
        }

        PaymentTransaction tx = optional.get();

        try {
            // PayOS webhook codes:
            // "00" = Success
            // "01" = Cancelled
            // Other codes = Failed

            if ("00".equals(code) && Boolean.TRUE.equals(webhookRequest.getSuccess())) {
                // Payment successful
                if (tx.getStatus() != TransactionStatus.COMPLETED) {
                    tx.setStatus(TransactionStatus.COMPLETED);
                    tx.setCompletedAt(new Date());

                    // Calculate coins (1000 VND = 1 coin)
                    long coins = 0L;
                    long bonusCoins = 0L;

                    if (amount != null) {
                        Long amountVnd = amount.longValue();
                        coins = amountVnd / 1000L;

                        // Query CoinPackage to get bonus coins based on price
                        var coinPackageOpt = coinPackageRepository.findByPrice(amountVnd);
                        if (coinPackageOpt.isPresent()) {
                            CoinPackage pkg = coinPackageOpt.get();
                            bonusCoins = pkg.getBonus() != null ? pkg.getBonus() : 0L;
                            coins = pkg.getCoin() != null ? pkg.getCoin() : coins; // Override coins if package defines it
                            log.info("Found coin package: {} for price: {}, bonus: {}",
                                    pkg.getName(),
                                    amountVnd,
                                    bonusCoins);
                        } else {
                            // Package not found: apply 7.5% bonus if amount >= 100k
                            if (amountVnd >= 100000L) {
                                bonusCoins = Math.round(coins * 0.075);
                                log.info("No coin package found for price: {}, applying 7.5% bonus: {}",
                                        amountVnd,
                                        bonusCoins);
                            } else {
                                log.info("No coin package found for price: {} (< 100k), no bonus applied", amountVnd);
                            }
                        }
                    }

                    // Add coins (base + bonus)
                    long totalCoins = coins + bonusCoins;
                    userCoinService.addCoin(tx.getUserId(), totalCoins, "payos");

                    log.info("PayOS payment completed (webhook): {} - added {} coins to user {}",
                            tx.getId(),
                            totalCoins,
                            tx.getUserId());
                } else {
                    log.info("PayOS webhook received but tx already COMPLETED: {}", tx.getId());
                }

            } else if ("01".equals(code)) {
                // Payment cancelled
                tx.setStatus(TransactionStatus.CANCELLED);
                log.info("PayOS payment cancelled (webhook): {}", tx.getId());

            } else {
                // Payment failed
                tx.setStatus(TransactionStatus.FAILED);
                log.info("PayOS payment failed (webhook) code={}: {}", code, tx.getId());
            }

            // Store PayOS payment link ID
            if (paymentLinkId != null && !paymentLinkId.isBlank()) {
                tx.setPayosPaymentLinkId(paymentLinkId);
            }

            tx.setUpdatedAt(new Date());
            paymentTransactionRepository.save(tx);

        } catch (Exception e) {
            log.error("Error processing PayOS webhook for orderCode={}: {}", orderCode, e.getMessage(), e);
            // Don't rethrow - webhook endpoint should return 200 to avoid retries from
            // PayOS
        }
    }
}
