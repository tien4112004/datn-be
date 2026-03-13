package com.datn.datnbe.ai.service;

import com.datn.datnbe.ai.dto.response.AdminStatsDto;
import com.datn.datnbe.ai.repository.TokenUsageRepository;
import com.datn.datnbe.auth.repository.UserProfileRepo;
import com.datn.datnbe.payment.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class AdminStatsService {

    static final int MONTHS_TO_SHOW = 7;
    static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    TokenUsageRepository tokenUsageRepository;
    UserProfileRepo userProfileRepo;
    PaymentTransactionRepository paymentTransactionRepository;

    public AdminStatsDto getStats() {
        var tokenStats = tokenUsageRepository.getStatsWithFilters(null, null, null, null);
        Long totalTokens = tokenStats != null ? tokenStats.getTotalTokens() : 0L;
        Long totalRequests = tokenStats != null ? tokenStats.getTotalRequests() : 0L;

        BigDecimal totalRevenue = paymentTransactionRepository.getTotalRevenue();
        Long totalTransactions = paymentTransactionRepository.getTotalCompletedTransactions();

        Date since = Date.from(Instant.now().minus((long) MONTHS_TO_SHOW * 30, ChronoUnit.DAYS));

        List<String> last7Months = buildLastNMonths(MONTHS_TO_SHOW);

        Map<String, Long> userRegMap = userProfileRepo.countRegistrationsByMonth(since)
                .stream()
                .collect(Collectors.toMap(r -> (String) r[0], r -> ((Number) r[1]).longValue()));
        List<AdminStatsDto.MonthlyCount> userRegistrationsByMonth = last7Months.stream()
                .map(m -> AdminStatsDto.MonthlyCount.builder().month(m).count(userRegMap.getOrDefault(m, 0L)).build())
                .toList();

        Map<String, Long> tokenMap = tokenUsageRepository.sumTokensByMonth(since)
                .stream()
                .collect(Collectors.toMap(r -> (String) r[0], r -> ((Number) r[1]).longValue()));
        List<AdminStatsDto.MonthlyTokens> tokenUsageByMonth = last7Months.stream()
                .map(m -> AdminStatsDto.MonthlyTokens.builder().month(m).tokens(tokenMap.getOrDefault(m, 0L)).build())
                .toList();

        Map<String, BigDecimal> revenueMap = paymentTransactionRepository.sumRevenueByMonth(since)
                .stream()
                .collect(Collectors.toMap(r -> (String) r[0], r -> new BigDecimal(r[1].toString())));
        List<AdminStatsDto.MonthlyRevenue> revenueByMonth = last7Months.stream()
                .map(m -> AdminStatsDto.MonthlyRevenue.builder()
                        .month(m)
                        .revenue(revenueMap.getOrDefault(m, BigDecimal.ZERO))
                        .build())
                .toList();

        return AdminStatsDto.builder()
                .totalTokens(totalTokens)
                .totalRequests(totalRequests)
                .totalRevenue(totalRevenue)
                .totalTransactions(totalTransactions)
                .userRegistrationsByMonth(userRegistrationsByMonth)
                .tokenUsageByMonth(tokenUsageByMonth)
                .revenueByMonth(revenueByMonth)
                .build();
    }

    private List<String> buildLastNMonths(int n) {
        YearMonth now = YearMonth.now();
        List<String> months = new ArrayList<>();
        for (int i = n - 1; i >= 0; i--) {
            months.add(now.minusMonths(i).format(MONTH_FORMATTER));
        }
        return months;
    }
}
