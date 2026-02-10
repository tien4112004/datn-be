package com.datn.datnbe.payment.service;

import com.datn.datnbe.payment.api.PaymentApi;
import com.datn.datnbe.payment.dto.CoinUsageTransactionDTO;
import com.datn.datnbe.payment.dto.UserCoinDTO;
import com.datn.datnbe.payment.entity.CoinUsageTransaction;
import com.datn.datnbe.payment.entity.UserCoin;
import com.datn.datnbe.payment.mapper.PaymentMapper;
import com.datn.datnbe.payment.repository.CoinUsageTransactionRepository;
import com.datn.datnbe.payment.repository.UserCoinRepository;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService implements PaymentApi {

    private final UserCoinRepository userCoinRepository;
    private final CoinUsageTransactionRepository transactionRepository;
    private final PaymentMapper mapper;

    @Value("${INIT_COIN_VALUE:0}")
    private Long initCoinValue;

    @Override
    @Transactional(readOnly = true)
    public UserCoinDTO getUserCoin(String userId) {
        UserCoin userCoin = userCoinRepository.findById(userId)
                .orElseGet(() -> UserCoin.builder()
                        .id(userId)
                        .coin(0L)
                        .build());
        return mapper.toUserCoinDTO(userCoin);
    }

    @Override
    public UserCoinDTO initializeUserCoin(String userId) {
        UserCoin userCoin = userCoinRepository.findById(userId)
                .orElseGet(() -> {
                    UserCoin newUserCoin = UserCoin.builder()
                            .id(userId)
                            .coin(initCoinValue)
                            .build();
                    return userCoinRepository.save(newUserCoin);
                });
        return mapper.toUserCoinDTO(userCoin);
    }

    @Override
    public UserCoinDTO subtractCoin(String userId, Long amount, String source) {
        UserCoin userCoin = userCoinRepository.findById(userId)
                .orElseGet(() -> UserCoin.builder()
                        .id(userId)
                        .coin(0L)
                        .build());

        userCoin.setCoin(userCoin.getCoin() - amount);
        userCoinRepository.save(userCoin);

        // Create transaction record
        CoinUsageTransaction transaction = CoinUsageTransaction.builder()
                .userId(userId)
                .type("subtract")
                .source(source)
                .amount(amount)
                .build();
        transactionRepository.save(transaction);

        return mapper.toUserCoinDTO(userCoin);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDto<CoinUsageTransactionDTO> getCoinHistory(
            String userId,
            String type,
            String source,
            Pageable pageable
    ) {
        List<CoinUsageTransaction> allTransactions = transactionRepository.findByUserIdOrderByCreatedAtDesc(userId);

        // Apply filters
        List<CoinUsageTransaction> filteredTransactions = allTransactions.stream()
                .filter(t -> type == null || type.isEmpty() || t.getType().equals(type))
                .filter(t -> source == null || source.isEmpty() || t.getSource().equals(source))
                .collect(Collectors.toList());

        // Apply pagination manually
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filteredTransactions.size());

        List<CoinUsageTransactionDTO> pageContent = filteredTransactions.subList(start, end)
                .stream()
                .map(mapper::toCoinUsageTransactionDTO)
                .collect(Collectors.toList());

        PaginationDto pagination = PaginationDto.builder()
                .currentPage(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .totalItems(filteredTransactions.size())
                .totalPages((filteredTransactions.size() + pageable.getPageSize() - 1) / pageable.getPageSize())
                .build();

        return PaginatedResponseDto.<CoinUsageTransactionDTO>builder()
                .data(pageContent)
                .pagination(pagination)
                .build();
    }
}
