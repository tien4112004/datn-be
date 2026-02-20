package com.datn.datnbe.payment.presentation;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.datn.datnbe.payment.api.PaymentApi;
import com.datn.datnbe.payment.dto.CoinUsageTransactionDto;
import com.datn.datnbe.payment.dto.UserCoinDto;
import com.datn.datnbe.payment.dto.request.CreateCheckoutRequest;
import com.datn.datnbe.payment.dto.request.SepayWebhookRequest;
import com.datn.datnbe.payment.dto.response.CheckoutResponse;
import com.datn.datnbe.payment.dto.response.TransactionDetailsDto;
import com.datn.datnbe.payment.service.PaymentService;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import com.datn.datnbe.sharedkernel.security.utils.SecurityContextUtils;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
        RequestMethod.DELETE})
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PaymentController {

    PaymentApi paymentApi;
    PaymentService paymentService;
    SecurityContextUtils securityContextUtils;

    @PostMapping("/checkout/create")
    public ResponseEntity<AppResponseDto<CheckoutResponse>> createCheckout(
            @Valid @RequestBody CreateCheckoutRequest request) {
        log.info("Request to create checkout with amount: {}", request.getAmount());

        String userId = securityContextUtils.getCurrentUserProfileId();
        CheckoutResponse response = paymentService.createCheckout(request, userId);

        return ResponseEntity.ok(AppResponseDto.<CheckoutResponse>builder()
                .data(response)
                .message("Checkout created successfully. Redirect to checkoutUrl")
                .build());
    }

    @GetMapping("/callback/success")
    public ResponseEntity<AppResponseDto<String>> handlePaymentSuccess(
            @RequestParam(required = false) String transactionId,
            @RequestParam(required = false) String referenceCode,
            @RequestParam(required = false, name = "order_invoice_number") String orderInvoiceNumber,
            @RequestParam(required = false, name = "order_id") String orderId) {

        log.info(
                "Payment success callback received: transactionId={}, referenceCode={}, orderInvoiceNumber={}, orderId={}",
                transactionId,
                referenceCode,
                orderInvoiceNumber,
                orderId);

        String resolvedTransactionId = transactionId;

        // Try to resolve transaction using reference code when transactionId is absent
        if (resolvedTransactionId == null && referenceCode != null) {
            try {
                TransactionDetailsDto tx = paymentApi.getTransactionByReferenceCode(referenceCode);
                if (tx != null) {
                    resolvedTransactionId = tx.getId();
                }
            } catch (Exception e) {
                log.warn("Could not resolve transaction by referenceCode={}", referenceCode, e);
            }
        }

        // If still not resolved, fall back to returning a friendly message (avoid HTTP
        // 500)
        if (resolvedTransactionId == null) {
            return ResponseEntity.ok(AppResponseDto.<String>builder()
                    .data(null)
                    .message(
                            "Callback received but no transaction identifier provided. Please check the redirect parameters.")
                    .build());
        }

        // Transaction status will be updated via IPN endpoint
        // This endpoint confirms the user completed payment and returned to app
        return ResponseEntity.ok(AppResponseDto.<String>builder()
                .data(resolvedTransactionId)
                .message("Payment completed. Please wait for confirmation.")
                .build());
    }

    @GetMapping("/callback/error")
    public ResponseEntity<AppResponseDto<String>> handlePaymentError(
            @RequestParam(required = false) String transactionId,
            @RequestParam(required = false) String referenceCode,
            @RequestParam(required = false, name = "order_invoice_number") String orderInvoiceNumber,
            @RequestParam(required = false) String code) {

        log.info("Payment error callback received: transactionId={}, referenceCode={}, orderInvoiceNumber={}, code={}",
                transactionId,
                referenceCode,
                orderInvoiceNumber,
                code);

        String resolvedTransactionId = transactionId;
        if (resolvedTransactionId == null && referenceCode != null) {
            try {
                TransactionDetailsDto tx = paymentApi.getTransactionByReferenceCode(referenceCode);
                if (tx != null) {
                    resolvedTransactionId = tx.getId();
                }
            } catch (Exception e) {
                log.warn("Could not resolve transaction by referenceCode={}", referenceCode, e);
            }
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(AppResponseDto.<String>builder()
                        .data(resolvedTransactionId)
                        .message("Payment failed. Please try again.")
                        .build());
    }

    @GetMapping("/callback/cancel")
    public ResponseEntity<AppResponseDto<String>> handlePaymentCancel(
            @RequestParam(required = false) String transactionId,
            @RequestParam(required = false) String referenceCode,
            @RequestParam(required = false, name = "order_invoice_number") String orderInvoiceNumber) {

        log.info("Payment cancel callback received: transactionId={}, referenceCode={}, orderInvoiceNumber={}",
                transactionId,
                referenceCode,
                orderInvoiceNumber);

        String resolvedTransactionId = transactionId;
        if (resolvedTransactionId == null && referenceCode != null) {
            try {
                TransactionDetailsDto tx = paymentApi.getTransactionByReferenceCode(referenceCode);
                if (tx != null) {
                    resolvedTransactionId = tx.getId();
                }
            } catch (Exception e) {
                log.warn("Could not resolve transaction by referenceCode={}", referenceCode, e);
            }
        }

        // If we can resolve a transaction id from the redirect, mark it CANCELLED
        // locally
        if (resolvedTransactionId != null) {
            try {
                paymentService.cancelTransaction(resolvedTransactionId);
            } catch (Exception e) {
                log.warn("Failed to mark transaction cancelled on redirect: {}", resolvedTransactionId, e);
            }
        }

        return ResponseEntity.ok(AppResponseDto.<String>builder()
                .data(resolvedTransactionId)
                .message("Payment cancelled by user.")
                .build());
    }

    @PostMapping("/notify/sepay")
    public ResponseEntity<AppResponseDto<Void>> handleSepayWebhook(@RequestBody SepayWebhookRequest webhookRequest) {
        String invoice = null;
        if (webhookRequest != null && webhookRequest.getOrder() != null) {
            invoice = webhookRequest.getOrder().getOrderInvoiceNumber();
        }
        log.info("Received Sepay webhook for order: {} payload={}", invoice, webhookRequest);

        paymentApi.handleSepayWebhook(webhookRequest);

        // Resolve transaction via service by orderInvoiceNumber
        String transactionId = "unknown";
        String status = "NOT_FOUND";
        if (invoice != null) {
            try {
                TransactionDetailsDto tx = paymentApi.getTransactionByOrderInvoiceNumber(invoice);
                if (tx != null) {
                    transactionId = tx.getId();
                    status = tx.getStatus() != null ? tx.getStatus() : "UNKNOWN";
                }
            } catch (Exception e) {
                log.debug("Could not resolve transaction by orderInvoiceNumber={} : {}", invoice, e.getMessage());
            }
        }

        String timestamp = java.time.ZonedDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy-HH:mm:ss"));
        String message = String.format("process successfully webhook - transactionid: %s, status: %s, timestamp: %s",
                transactionId,
                status,
                timestamp);

        return ResponseEntity.ok(AppResponseDto.<Void>builder().message(message).build());
    }

    @PostMapping("/notify/payos")
    public ResponseEntity<AppResponseDto<Void>> handlePayosWebhook(
            @RequestBody com.datn.datnbe.payment.dto.request.PayosWebhookRequest webhookRequest) {
        Long orderCode = null;
        if (webhookRequest != null && webhookRequest.getData() != null) {
            orderCode = webhookRequest.getData().getOrderCode();
        }
        log.info("Received PayOS webhook for order code: {} payload={}", orderCode, webhookRequest);

        paymentApi.handlePayosWebhook(webhookRequest);

        // Resolve transaction via service by orderCode (we store PayOS orderCode in
        // orderInvoiceNumber)
        String transactionId = "unknown";
        String status = "NOT_FOUND";
        if (orderCode != null) {
            try {
                TransactionDetailsDto tx = paymentApi.getTransactionByOrderInvoiceNumber(String.valueOf(orderCode));
                if (tx != null) {
                    transactionId = tx.getId();
                    status = tx.getStatus() != null ? tx.getStatus() : "UNKNOWN";
                }
            } catch (Exception e) {
                log.debug("Could not resolve transaction by orderCode={} : {}", orderCode, e.getMessage());
            }
        }

        String timestamp = java.time.ZonedDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy-HH:mm:ss"));
        String message = String.format("process successfully webhook - transactionid: %s, status: %s, timestamp: %s",
                transactionId,
                status,
                timestamp);

        return ResponseEntity.ok(AppResponseDto.<Void>builder().message(message).build());
    }

    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<AppResponseDto<TransactionDetailsDto>> getTransactionDetails(
            @PathVariable String transactionId) {
        log.info("Fetching transaction details for ID: {}", transactionId);

        TransactionDetailsDto transaction = paymentApi.getTransactionDetails(transactionId);

        return ResponseEntity.ok(AppResponseDto.<TransactionDetailsDto>builder()
                .data(transaction)
                .message("Transaction retrieved successfully")
                .build());
    }

    @GetMapping("/transaction/reference/{referenceCode}")
    public ResponseEntity<AppResponseDto<TransactionDetailsDto>> getTransactionByReferenceCode(
            @PathVariable String referenceCode) {
        log.info("Fetching transaction by reference code: {}", referenceCode);

        TransactionDetailsDto transaction = paymentApi.getTransactionByReferenceCode(referenceCode);

        return ResponseEntity.ok(AppResponseDto.<TransactionDetailsDto>builder()
                .data(transaction)
                .message("Transaction retrieved successfully")
                .build());
    }

    @GetMapping("/user/transactions")
    public ResponseEntity<AppResponseDto<PaginatedResponseDto<TransactionDetailsDto>>> getUserTransactions(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        log.info("Fetching user transactions - page: {}, size: {}", page, size);

        String userId = securityContextUtils.getCurrentUserProfileId();
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        PaginatedResponseDto<TransactionDetailsDto> transactions = paymentApi.getUserTransactions(userId, pageable);

        return ResponseEntity.ok(AppResponseDto.<PaginatedResponseDto<TransactionDetailsDto>>builder()
                .data(transactions)
                .message("User transactions retrieved successfully")
                .build());
    }

    @GetMapping("/{userId}/coins")
    public ResponseEntity<UserCoinDto> getUserCoin(@PathVariable String userId) {
        return ResponseEntity.ok(paymentApi.getUserCoin(userId));
    }

    @GetMapping("/{userId}/history")
    public ResponseEntity<PaginatedResponseDto<CoinUsageTransactionDto>> getCoinHistory(@PathVariable String userId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String source,
            @PageableDefault(size = 20, page = 0, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(paymentApi.getCoinHistory(userId, type, source, pageable));
    }
}
