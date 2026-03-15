package com.datn.datnbe.payment.dto.response;

import java.math.BigDecimal;
import java.util.Date;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminTransactionDto {

    String id;
    String userId;
    BigDecimal amount;
    String description;
    String referenceCode;
    String status;
    String gate;
    Date createdAt;
    Date completedAt;
    Date updatedAt;
}
