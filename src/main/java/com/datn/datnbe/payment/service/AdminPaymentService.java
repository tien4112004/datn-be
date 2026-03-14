package com.datn.datnbe.payment.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.datn.datnbe.payment.dto.response.AdminDailyTotalAmountDto;
import com.datn.datnbe.payment.dto.response.AdminTransactionDto;
import com.datn.datnbe.payment.entity.ExchangeRate;
import com.datn.datnbe.payment.entity.PaymentTransaction.TransactionStatus;
import com.datn.datnbe.payment.repository.ExchangeRateRepository;
import com.datn.datnbe.payment.repository.PaymentTransactionRepository;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginationDto;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminPaymentService {

    PaymentTransactionRepository paymentTransactionRepository;
    ExchangeRateRepository exchangeRateRepository;

    public PaginatedResponseDto<AdminTransactionDto> getAllTransactions(int page, int size, String status) {
        TransactionStatus statusFilter = null;
        if (status != null && !status.isBlank()) {
            statusFilter = TransactionStatus.valueOf(status.toUpperCase());
        }

        var pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<AdminTransactionDto> result = paymentTransactionRepository.findAllWithFilter(statusFilter, pageable)
                .map(t -> AdminTransactionDto.builder()
                        .id(t.getId())
                        .userId(t.getUserId())
                        .amount(t.getAmount())
                        .description(t.getDescription())
                        .referenceCode(t.getReferenceCode())
                        .status(t.getStatus() != null ? t.getStatus().name() : null)
                        .gate(t.getGate())
                        .createdAt(t.getCreatedAt())
                        .completedAt(t.getCompletedAt())
                        .updatedAt(t.getUpdatedAt())
                        .build());

        return PaginatedResponseDto.<AdminTransactionDto>builder()
                .data(result.getContent())
                .pagination(PaginationDto.builder()
                        .currentPage(page)
                        .pageSize(size)
                        .totalItems(result.getTotalElements())
                        .totalPages(result.getTotalPages())
                        .build())
                .build();
    }

    public List<AdminDailyTotalAmountDto> getTotalAmountsByDateRange(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "startDate and endDate are required");
        }

        Date normalizedStartDate = atStartOfDay(startDate);
        Date normalizedEndDate = atStartOfDay(endDate);
        if (normalizedStartDate.after(normalizedEndDate)) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "startDate must be before or equal to endDate");
        }

        Date toDateExclusive = addDays(normalizedEndDate, 1);

        return paymentTransactionRepository.sumCompletedAmountByDateRange(normalizedStartDate, toDateExclusive)
                .stream()
                .map(row -> AdminDailyTotalAmountDto.builder()
                .date(resolveLocalDate(row[0]))
                        .totalAmount(resolveBigDecimal(row[1]))
                        .build())
                .toList();
    }

    public List<AdminDailyTotalAmountDto> getTotalCostByDateRange(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "startDate and endDate are required");
        }

        Date normalizedStartDate = atStartOfDay(startDate);
        Date normalizedEndDate = atStartOfDay(endDate);
        if (normalizedStartDate.after(normalizedEndDate)) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "startDate must be before or equal to endDate");
        }

        Date toDateExclusive = addDays(normalizedEndDate, 1);
        LocalDate endLocalDate = toLocalDate(normalizedEndDate);
        boolean isCurrentOpenMonth = endLocalDate.equals(LocalDate.now()) && !isLastDayOfMonth(endLocalDate);

        Map<YearMonth, BigDecimal> rateByMonth = new HashMap<>();
        BigDecimal latestRate = isCurrentOpenMonth ? resolveLatestRate() : null;

        return paymentTransactionRepository.sumCostByDateRange(normalizedStartDate, toDateExclusive)
                .stream()
                .map(row -> {
                    LocalDate transactionDate = resolveLocalDate(row[0]);
                    BigDecimal totalAmountUsd = resolveBigDecimal(row[1]);
                    YearMonth transactionMonth = YearMonth.from(transactionDate);

                    BigDecimal rate = rateByMonth.computeIfAbsent(transactionMonth,
                            month -> resolveApplicableRate(month, endLocalDate, isCurrentOpenMonth, latestRate));

                    return AdminDailyTotalAmountDto.builder()
                            .date(transactionDate)
                            .totalAmount(totalAmountUsd.multiply(rate))
                            .build();
                })
                .toList();
    }

    private BigDecimal resolveApplicableRate(
            YearMonth month,
            LocalDate endLocalDate,
            boolean isCurrentOpenMonth,
            BigDecimal latestRate) {
        YearMonth endMonth = YearMonth.from(endLocalDate);
        if (isCurrentOpenMonth && month.equals(endMonth)) {
            return latestRate;
        }

        LocalDate monthEndDate = month.atEndOfMonth();
        ExchangeRate exchangeRate = exchangeRateRepository.findByDate(monthEndDate);
        if (exchangeRate == null) {
            exchangeRate = exchangeRateRepository.findTopByDateLessThanEqualOrderByDateDesc(monthEndDate);
        }

        if (exchangeRate == null || exchangeRate.getRate() == null) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "Missing exchange rate for month " + month);
        }

        return BigDecimal.valueOf(exchangeRate.getRate());
    }

    private BigDecimal resolveLatestRate() {
        ExchangeRate latestExchangeRate = exchangeRateRepository.findTopByOrderByDateDesc();
        if (latestExchangeRate == null || latestExchangeRate.getRate() == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "No exchange rate found in database");
        }
        return BigDecimal.valueOf(latestExchangeRate.getRate());
    }

    private LocalDate resolveLocalDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value instanceof Date date) {
            return toLocalDate(date);
        }
        return LocalDate.parse(value.toString());
    }

    private BigDecimal resolveBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        return new BigDecimal(value.toString());
    }

    private Date atStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private Date addDays(Date date, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, days);
        return calendar.getTime();
    }

    private LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private boolean isLastDayOfMonth(LocalDate date) {
        return date.getDayOfMonth() == date.lengthOfMonth();
    }
}
