package com.datn.datnbe.payment.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
import com.datn.datnbe.payment.entity.CoinUsageTransaction;
import com.datn.datnbe.payment.entity.PaymentTransaction;
import com.datn.datnbe.payment.entity.PaymentTransaction.TransactionStatus;
import com.datn.datnbe.payment.entity.UserCoin;
import com.datn.datnbe.payment.mapper.PaymentMapper;
import com.datn.datnbe.payment.repository.CoinUsageTransactionRepository;
import com.datn.datnbe.payment.repository.PaymentTransactionRepository;
import com.datn.datnbe.payment.repository.UserCoinRepository;
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
    private final UserCoinRepository userCoinRepository;
    private final CoinUsageTransactionRepository coinUsageTransactionRepository;
    private final PaymentMapper mapper;

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
                    null // paymentMethod - let user choose on Sepay page
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
        String orderInvoice = null;
        String orderStatus = null;
        java.math.BigDecimal orderAmount = null;
        String orderId = null;

        // Extract values safely from nested payload
        if (webhookRequest != null && webhookRequest.getOrder() != null) {
            SepayWebhookRequest.Order order = webhookRequest.getOrder();
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

        try {
            if (orderInvoice == null) {
                log.warn("Webhook received without order_invoice_number: {}", webhookRequest);
                return; // Ignore malformed webhook
            }

            // Find transaction by order invoice number
            var optionalTransaction = paymentTransactionRepository.findByOrderInvoiceNumber(orderInvoice);
            if (optionalTransaction.isEmpty()) {
                // Don't throw - log and return to avoid 500 responses to Sepay
                log.warn("Transaction not found for order invoice: {}. Ignoring webhook.", orderInvoice);
                return;
            }

            PaymentTransaction transaction = optionalTransaction.get();

            // Update transaction status based on Sepay order status
            if ("CAPTURED".equalsIgnoreCase(orderStatus)) {
                transaction.setStatus(TransactionStatus.COMPLETED);
                transaction.setCompletedAt(new Date());

                // Add coins to user account when payment is completed
                Long coinAmount = 0L;
                if (orderAmount != null) {
                    coinAmount = orderAmount.longValue() / 1000; // 1000 VND = 1 coin
                }

                addCoin(transaction.getUserId(), coinAmount, "sepay");

                log.info("Payment completed: {} - Added {} coins to user {}",
                        transaction.getId(),
                        coinAmount,
                        transaction.getUserId());
            } else if ("CANCELLED".equalsIgnoreCase(orderStatus)) {
                transaction.setStatus(TransactionStatus.CANCELLED);
                log.info("Payment cancelled: {}", transaction.getId());
            } else if ("AUTHENTICATION_NOT_NEEDED".equalsIgnoreCase(orderStatus)) {
                transaction.setStatus(TransactionStatus.PENDING);
                log.info("Payment pending: {}", transaction.getId());
            } else {
                transaction.setStatus(TransactionStatus.FAILED);
                log.info("Payment failed with status: {}", orderStatus);
            }

            // Store Sepay order ID and transaction ID
            if (orderId != null) {
                transaction.setSepayTransactionId(orderId);
            }
            // Also store Sepay transaction id if present
            if (webhookRequest.getTransaction() != null && webhookRequest.getTransaction().getTransactionId() != null) {
                transaction.setSepayTransactionId(webhookRequest.getTransaction().getTransactionId());
            }

            transaction.setUpdatedAt(new Date());
            paymentTransactionRepository.save(transaction);

            log.info("Successfully processed Sepay webhook for transaction: {}", transaction.getId());

        } catch (AppException e) {
            // keep throwing application exceptions
            throw e;
        } catch (Exception e) {
            // Catch-all: log and avoid throwing to prevent 500 to Sepay
            log.error("Error processing Sepay webhook (will not rethrow): {}", e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionDetailsDto getTransactionDetails(String transactionId) {
        PaymentTransaction transaction = paymentTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Transaction not found"));

        return mapToTransactionDetailsDto(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDto<TransactionDetailsDto> getUserTransactions(String userId, Pageable pageable) {
        Page<PaymentTransaction> transactions = paymentTransactionRepository.findByUserId(userId, pageable);
        return PaginatedResponseDto.<TransactionDetailsDto>builder()
                .data(transactions.map(this::mapToTransactionDetailsDto).getContent())
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

        return mapToTransactionDetailsDto(transaction);
    }

    private String appendCallbackParams(String url, String transactionId, String referenceCode) {
        if (url == null || url.isBlank()) {
            return url;
        }
        String join = url.contains("?") ? "&" : "?";
        return url + join + "transactionId=" + URLEncoder.encode(transactionId, StandardCharsets.UTF_8)
                + "&referenceCode=" + URLEncoder.encode(referenceCode, StandardCharsets.UTF_8);
    }

    private TransactionDetailsDto mapToTransactionDetailsDto(PaymentTransaction transaction) {
        return TransactionDetailsDto.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .description(transaction.getDescription())
                .referenceCode(transaction.getReferenceCode())
                .status(transaction.getStatus().toString())
                .gate(transaction.getGate())
                .createdAt(transaction.getCreatedAt())
                .completedAt(transaction.getCompletedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }

    private String generateReferenceCode() {
        return "REF" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    @Override
    @Transactional(readOnly = true)
    public UserCoinDto getUserCoin(String userId) {
        UserCoin userCoin = userCoinRepository.findById(userId)
                .orElseGet(() -> UserCoin.builder().id(userId).coin(0L).build());
        return mapper.toUserCoinDTO(userCoin);
    }

    @Override
    public UserCoinDto initializeUserCoin(String userId) {
        UserCoin userCoin = userCoinRepository.findById(userId).orElseGet(() -> {
            UserCoin newUserCoin = UserCoin.builder().id(userId).coin(initCoinValue).build();
            return userCoinRepository.save(newUserCoin);
        });
        return mapper.toUserCoinDTO(userCoin);
    }

    @Override
    public UserCoinDto subtractCoin(String userId, Long amount, String source) {
        UserCoin userCoin = userCoinRepository.findById(userId)
                .orElseGet(() -> UserCoin.builder().id(userId).coin(0L).build());

        userCoin.setCoin(userCoin.getCoin() - amount);
        userCoinRepository.save(userCoin);

        // Create transaction record
        CoinUsageTransaction transaction = CoinUsageTransaction.builder()
                .userId(userId)
                .type("subtract")
                .source(source)
                .amount(amount)
                .build();
        coinUsageTransactionRepository.save(transaction);
        log.info("Created coin usage transaction for user: {}, {} {} coins",
                transaction.getUserId(),
                transaction.getType(),
                transaction.getAmount());

        return mapper.toUserCoinDTO(userCoin);
    }

    @Override
    public UserCoinDto addCoin(String userId, Long amount, String source) {
        UserCoin userCoin = userCoinRepository.findById(userId)
                .orElseGet(() -> UserCoin.builder().id(userId).coin(0L).build());
        userCoin.setCoin(userCoin.getCoin() + amount);
        userCoinRepository.save(userCoin);
        // Create transaction record
        CoinUsageTransaction transaction = CoinUsageTransaction.builder()
                .userId(userId)
                .type("add")
                .source(source)
                .amount(amount)
                .build();
        coinUsageTransactionRepository.save(transaction);
        log.info("Created coin usage transaction for user: {}, {} {} coins",
                transaction.getUserId(),
                transaction.getType(),
                transaction.getAmount());
        return mapper.toUserCoinDTO(userCoin);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDto<CoinUsageTransactionDto> getCoinHistory(String userId,
            String type,
            String source,
            Pageable pageable) {
        List<CoinUsageTransaction> allTransactions = coinUsageTransactionRepository
                .findByUserIdOrderByCreatedAtDesc(userId);
        // Apply filters
        List<CoinUsageTransaction> filteredTransactions = allTransactions.stream()
                .filter(t -> type == null || type.isEmpty() || t.getType().equals(type))
                .filter(t -> source == null || source.isEmpty() || t.getSource().equals(source))
                .collect(Collectors.toList());

        // Apply pagination manually
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filteredTransactions.size());

        List<CoinUsageTransactionDto> pageContent = filteredTransactions.subList(start, end)
                .stream()
                .map(mapper::toCoinUsageTransactionDTO)
                .collect(Collectors.toList());

        PaginationDto pagination = PaginationDto.builder()
                .currentPage(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .totalItems(filteredTransactions.size())
                .totalPages((filteredTransactions.size() + pageable.getPageSize() - 1) / pageable.getPageSize())
                .build();

        return PaginatedResponseDto.<CoinUsageTransactionDto>builder().data(pageContent).pagination(pagination).build();
    }
}
