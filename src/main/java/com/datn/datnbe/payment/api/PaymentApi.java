package com.datn.datnbe.payment.api;

import com.datn.datnbe.payment.dto.CoinUsageTransactionDto;
import com.datn.datnbe.payment.dto.UserCoinDto;
import com.datn.datnbe.payment.dto.request.PayosWebhookRequest;
import com.datn.datnbe.payment.dto.request.SepayWebhookRequest;
import com.datn.datnbe.payment.dto.response.TransactionDetailsDto;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import org.springframework.data.domain.Pageable;

public interface PaymentApi {

    UserCoinDto getUserCoin(String userId);

    UserCoinDto initializeUserCoin(String userId);

    UserCoinDto subtractCoin(String userId, Long amount, String source);

    UserCoinDto addCoin(String userId, Long amount, String source);

    PaginatedResponseDto<CoinUsageTransactionDto> getCoinHistory(String userId,
            String type,
            String source,
            Pageable pageable);

    void handleSepayWebhook(SepayWebhookRequest webhookRequest);

    void handlePayosWebhook(PayosWebhookRequest webhookRequest);

    TransactionDetailsDto getTransactionDetails(String transactionId);

    PaginatedResponseDto<TransactionDetailsDto> getUserTransactions(String userId, Pageable pageable);

    TransactionDetailsDto getTransactionByReferenceCode(String referenceCode);

    /**
     * Find a transaction using the stored order invoice number (used by Sepay and
     * PayOS orderCode)
     */
    TransactionDetailsDto getTransactionByOrderInvoiceNumber(String orderInvoiceNumber);
}
