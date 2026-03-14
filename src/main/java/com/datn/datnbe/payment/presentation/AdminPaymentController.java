package com.datn.datnbe.payment.presentation;

import com.datn.datnbe.payment.dto.response.AdminTransactionDto;
import com.datn.datnbe.payment.entity.PaymentTransaction.TransactionStatus;
import com.datn.datnbe.payment.repository.PaymentTransactionRepository;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginationDto;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/admin/transactions")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class AdminPaymentController {

    PaymentTransactionRepository paymentTransactionRepository;

    @GetMapping
    public ResponseEntity<AppResponseDto<PaginatedResponseDto<AdminTransactionDto>>> getAllTransactions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {

        log.info("Admin fetching all transactions - page: {}, size: {}, status: {}", page, size, status);

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

        return ResponseEntity.ok(AppResponseDto.success(PaginatedResponseDto.<AdminTransactionDto>builder()
                .data(result.getContent())
                .pagination(PaginationDto.builder()
                        .currentPage(page)
                        .pageSize(size)
                        .totalItems(result.getTotalElements())
                        .totalPages(result.getTotalPages())
                        .build())
                .build()));
    }
}
