package com.datn.datnbe.payment.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.datn.datnbe.payment.api.PaymentApi;
import com.datn.datnbe.payment.apiclient.PaymentGatewayClient;
import com.datn.datnbe.payment.dto.CoinUsageTransactionDto;
import com.datn.datnbe.payment.dto.UserCoinDto;
import com.datn.datnbe.payment.dto.request.CreateCheckoutRequest;
import com.datn.datnbe.payment.dto.request.SepayWebhookRequest;
import com.datn.datnbe.payment.dto.response.CheckoutResponse;
import com.datn.datnbe.payment.dto.response.TransactionDetailsDto;
import com.datn.datnbe.payment.entity.PaymentTransaction;
import com.datn.datnbe.payment.entity.PaymentTransaction.TransactionStatus;
import com.datn.datnbe.payment.mapper.PaymentMapper;
import com.datn.datnbe.payment.repository.PaymentTransactionRepository;

import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginationDto;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentService implements PaymentApi {

    PaymentTransactionRepository paymentTransactionRepository;
    PaymentGatewayClient paymentGatewayClient;
    ObjectMapper objectMapper;
    private final PaymentMapper mapper;
    private final com.datn.datnbe.payment.service.UserCoinService userCoinService;

    private final SepayWebhookService sepayWebhookService;
    @Value("${app.coin.initial:0}")
    @NonFinal
    private Long initCoinValue;

    @Transactional
    public CheckoutResponse createCheckout(CreateCheckoutRequest request, String userId) {
        log.info("Creating checkout for user: {} with amount: {}", userId, request.getAmount());

        try {
            // Generate unique reference code
            String referenceCode = request.getReferenceCode() != null
                    ? request.getReferenceCode()
                    : generateReferenceCode();

            // Generate order invoice number for Sepay (format: DH + timestamp)
            String orderInvoiceNumber = "DH" + System.currentTimeMillis();

            // Determine payment gateway
            String gate = request.getGate() != null ? request.getGate() : "SEPAY";

            // Prepare transaction record and save BEFORE calling external gateway
            // so we can include our internal transaction id in the redirect URLs.
            PaymentTransaction transaction = PaymentTransaction.builder()
                    .userId(userId)
                    .amount(request.getAmount())
                    .description(request.getDescription())
                    .referenceCode(referenceCode)
                    .orderInvoiceNumber(orderInvoiceNumber) // Store Sepay order invoice number
                    .status(TransactionStatus.PENDING)
                    .gate(gate)
                    .createdAt(new Date())
                    .updatedAt(new Date())
                    .build();

            PaymentTransaction savedTransaction = paymentTransactionRepository.save(transaction);

            // Append transaction identifiers to provided callback URLs so Sepay redirect will include them
            String successUrl = appendCallbackParams(request.getSuccessUrl(), savedTransaction.getId(), referenceCode);
            String errorUrl = appendCallbackParams(request.getErrorUrl(), savedTransaction.getId(), referenceCode);
            String cancelUrl = appendCallbackParams(request.getCancelUrl(), savedTransaction.getId(), referenceCode);

            // Create checkout with Sepay Payment Gateway
            // Pass null for customerId and paymentMethod to let user choose
            CheckoutResponse checkoutResponse = paymentGatewayClient.createCheckout(orderInvoiceNumber,
                    request.getAmount(),
                    request.getDescription(),
                    userId, // customerId
                    successUrl,
                    errorUrl,
                    cancelUrl,
                    "BANK_TRANSFER" // force BANK_TRANSFER so FE doesn't need to send paymentMethod
            );

            // Update transaction with returned checkout data for auditing
            savedTransaction.setTransactionData(objectMapper.writeValueAsString(checkoutResponse));
            savedTransaction.setUpdatedAt(new Date());
            paymentTransactionRepository.save(savedTransaction);

            // Update response with the database transaction ID and reference code
            checkoutResponse.setTransactionId(savedTransaction.getId());
            checkoutResponse.setReferenceCode(referenceCode);

            log.info("Successfully created checkout with ID: {} and order invoice: {} using gateway: {}",
                    savedTransaction.getId(),
                    orderInvoiceNumber,
                    gate);

            return checkoutResponse;

        } catch (Exception e) {
            log.error("Error creating checkout: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to create checkout");
        }
    }

    @Override
    @Transactional
    public void handleSepayWebhook(SepayWebhookRequest webhookRequest) {
        // delegate to extracted service to keep PaymentService minimal
        sepayWebhookService.handle(webhookRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionDetailsDto getTransactionDetails(String transactionId) {
        PaymentTransaction transaction = paymentTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Transaction not found"));

        return mapper.toTransactionDetailsDto(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDto<TransactionDetailsDto> getUserTransactions(String userId, Pageable pageable) {
        Page<PaymentTransaction> transactions = paymentTransactionRepository.findByUserId(userId, pageable);
        return PaginatedResponseDto.<TransactionDetailsDto>builder()
                .data(transactions.map(mapper::toTransactionDetailsDto).getContent())
                .pagination(PaginationDto.builder()
                        .currentPage(pageable.getPageNumber() + 1)
                        .pageSize(transactions.getSize())
                        .totalPages(transactions.getTotalPages())
                        .totalItems(transactions.getTotalElements())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionDetailsDto getTransactionByReferenceCode(String referenceCode) {
        PaymentTransaction transaction = paymentTransactionRepository.findByReferenceCode(referenceCode)
                .orElseThrow(() -> new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Transaction not found"));

        return mapper.toTransactionDetailsDto(transaction);
    }

    @Transactional
    public void cancelTransaction(String transactionId) {
        var optionalTransaction = paymentTransactionRepository.findById(transactionId);
        if (optionalTransaction.isEmpty()) {
            log.warn("Cancel requested but transaction not found: {}", transactionId);
            return;
        }

        PaymentTransaction transaction = optionalTransaction.get();

        // Do not cancel a completed transaction
        if (transaction.getStatus() == TransactionStatus.COMPLETED) {
            log.warn("Attempt to cancel already completed transaction: {}", transactionId);
            return;
        }

        transaction.setStatus(TransactionStatus.CANCELLED);
        transaction.setUpdatedAt(new Date());
        paymentTransactionRepository.save(transaction);
        log.info("Transaction marked as CANCELLED (by user redirect): {}", transactionId);

        // Try to notify Sepay (best-effort) to cancel the order on their side
        try {
            if (transaction.getOrderInvoiceNumber() != null && !transaction.getOrderInvoiceNumber().isBlank()) {
                boolean cancelled = paymentGatewayClient.cancelOrder(transaction.getOrderInvoiceNumber());
                log.info("Requested Sepay cancel for order {} result={}",
                        transaction.getOrderInvoiceNumber(),
                        cancelled);
            }
        } catch (Exception e) {
            log.warn("Failed to call Sepay cancel for order {}: {}",
                    transaction.getOrderInvoiceNumber(),
                    e.getMessage());
        }
    }

    private String appendCallbackParams(String url, String transactionId, String referenceCode) {
        if (url == null || url.isBlank()) {
            return url;
        }
        String join = url.contains("?") ? "&" : "?";
        return url + join + "transactionId=" + URLEncoder.encode(transactionId, StandardCharsets.UTF_8)
                + "&referenceCode=" + URLEncoder.encode(referenceCode, StandardCharsets.UTF_8);
    }

    private String generateReferenceCode() {
        return "REF" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    @Override
    @Transactional(readOnly = true)
    public UserCoinDto getUserCoin(String userId) {
        return userCoinService.getUserCoin(userId);
    }

    @Override
    public UserCoinDto initializeUserCoin(String userId) {
        return userCoinService.initializeUserCoin(userId, initCoinValue);
    }

    @Override
    public UserCoinDto subtractCoin(String userId, Long amount, String source) {
        return userCoinService.subtractCoin(userId, amount, source);
    }

    @Override
    public UserCoinDto addCoin(String userId, Long amount, String source) {
        return userCoinService.addCoin(userId, amount, source);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDto<CoinUsageTransactionDto> getCoinHistory(String userId,
            String type,
            String source,
            Pageable pageable) {
        return userCoinService.getCoinHistory(userId, type, source, pageable);
    }
}
