package com.datn.datnbe.payment.presentation;

import com.datn.datnbe.payment.api.PaymentApi;
import com.datn.datnbe.payment.dto.CoinUsageTransactionDTO;
import com.datn.datnbe.payment.dto.UserCoinDTO;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for payment endpoints.
 */
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentApi paymentApi;

    @GetMapping("/{userId}/coins")
    public ResponseEntity<UserCoinDTO> getUserCoin(@PathVariable String userId) {
        return ResponseEntity.ok(paymentApi.getUserCoin(userId));
    }
    
    @GetMapping("/{userId}/history")
    public ResponseEntity<PaginatedResponseDto<CoinUsageTransactionDTO>> getCoinHistory(
            @PathVariable String userId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String source,
            @PageableDefault(size = 20, page = 0, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(paymentApi.getCoinHistory(userId, type, source, pageable));
    }
}
