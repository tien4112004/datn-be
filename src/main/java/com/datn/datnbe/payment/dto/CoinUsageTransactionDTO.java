package com.datn.datnbe.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoinUsageTransactionDTO {
    private String id;
    private String userId;
    private LocalDateTime createdAt;
    private String type;
    private String source;
    private Long amount;
}
