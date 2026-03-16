package com.datn.datnbe.payment.presentation;

import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import com.datn.datnbe.payment.dto.response.AdminTransactionDto;
import com.datn.datnbe.payment.dto.response.AdminDailyTotalAmountDto;
import com.datn.datnbe.payment.service.AdminPaymentService;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class AdminPaymentController {

    AdminPaymentService adminPaymentService;

    @GetMapping("/transactions")
    public ResponseEntity<AppResponseDto<PaginatedResponseDto<AdminTransactionDto>>> getAllTransactions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {

        log.info("Admin fetching all transactions - page: {}, size: {}, status: {}", page, size, status);

        PaginatedResponseDto<AdminTransactionDto> result = adminPaymentService.getAllTransactions(page, size, status);

        return ResponseEntity.ok(AppResponseDto.success(result));
    }

    @GetMapping("/finance/revenue-by-date")
    public ResponseEntity<AppResponseDto<List<AdminDailyTotalAmountDto>>> getTotalAmountsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) {

        log.info("Admin fetching total amounts - startDate: {}, endDate: {}", startDate, endDate);

        List<AdminDailyTotalAmountDto> result = adminPaymentService.getTotalAmountsByDateRange(startDate, endDate);
        return ResponseEntity.ok(AppResponseDto.success(result));
    }

    @GetMapping("/finance/cost-by-date")
    public ResponseEntity<AppResponseDto<List<AdminDailyTotalAmountDto>>> getTotalCostByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) {

        log.info("Admin fetching total cost - startDate: {}, endDate: {}", startDate, endDate);

        List<AdminDailyTotalAmountDto> result = adminPaymentService.getTotalCostByDateRange(startDate, endDate);
        return ResponseEntity.ok(AppResponseDto.success(result));
    }

}
