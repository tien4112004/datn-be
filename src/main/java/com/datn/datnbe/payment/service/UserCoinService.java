package com.datn.datnbe.payment.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.datn.datnbe.payment.dto.CoinUsageTransactionDto;
import com.datn.datnbe.payment.dto.UserCoinDto;
import com.datn.datnbe.payment.entity.CoinUsageTransaction;
import com.datn.datnbe.payment.entity.UserCoin;
import com.datn.datnbe.payment.mapper.PaymentMapper;
import com.datn.datnbe.payment.repository.CoinUsageTransactionRepository;
import com.datn.datnbe.payment.repository.UserCoinRepository;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginationDto;

import lombok.RequiredArgsConstructor;

/**
 * Small service extracted from PaymentService to manage user coin operations.
 */
@Service
@RequiredArgsConstructor
public class UserCoinService {

    private final UserCoinRepository userCoinRepository;
    private final CoinUsageTransactionRepository coinUsageTransactionRepository;
    private final PaymentMapper mapper;

    @Transactional(readOnly = true)
    public UserCoinDto getUserCoin(String userId) {
        UserCoin userCoin = userCoinRepository.findById(userId)
                .orElseGet(() -> UserCoin.builder().id(userId).coin(0L).build());
        return mapper.toUserCoinDTO(userCoin);
    }

    @Transactional
    public UserCoinDto initializeUserCoin(String userId, Long initialValue) {
        UserCoin userCoin = userCoinRepository.findById(userId).orElseGet(() -> {
            UserCoin newUserCoin = UserCoin.builder().id(userId).coin(initialValue).build();
            return userCoinRepository.save(newUserCoin);
        });
        return mapper.toUserCoinDTO(userCoin);
    }

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

    @Transactional
    public UserCoinDto subtractCoin(String userId, Long amount, String source) {
        UserCoin userCoin = userCoinRepository.findById(userId)
                .orElseGet(() -> UserCoin.builder().id(userId).coin(0L).build());

        userCoin.setCoin(userCoin.getCoin() - amount);
        userCoinRepository.save(userCoin);

        CoinUsageTransaction transaction = CoinUsageTransaction.builder()
                .userId(userId)
                .type("subtract")
                .source(source)
                .amount(amount)
                .build();
        coinUsageTransactionRepository.save(transaction);

        return mapper.toUserCoinDTO(userCoin);
    }

    @Transactional
    public UserCoinDto addCoin(String userId, Long amount, String source) {
        UserCoin userCoin = userCoinRepository.findById(userId)
                .orElseGet(() -> UserCoin.builder().id(userId).coin(0L).build());
        userCoin.setCoin(userCoin.getCoin() + amount);
        userCoinRepository.save(userCoin);

        CoinUsageTransaction transaction = CoinUsageTransaction.builder()
                .userId(userId)
                .type("add")
                .source(source)
                .amount(amount)
                .build();
        coinUsageTransactionRepository.save(transaction);

        return mapper.toUserCoinDTO(userCoin);
    }
}
