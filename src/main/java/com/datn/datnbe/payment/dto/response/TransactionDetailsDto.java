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
public class TransactionDetailsDto {

    private String id;

    private BigDecimal amount;

    private String description;

    private String referenceCode;

    private String status;

    private String gate;

    private Date createdAt;

    private Date completedAt;

    private Date updatedAt;
}
