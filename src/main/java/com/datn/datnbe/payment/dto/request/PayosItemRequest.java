package com.datn.datnbe.payment.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PayOS item request DTO
 * Represents an item in a PayOS payment request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayosItemRequest {

    /**
     * Item name
     */
    private String name;

    /**
     * Item quantity
     */
    private Integer quantity;

    /**
     * Item price (in VND)
     */
    private Integer price;

    /**
     * Item unit (optional)
     */
    private String unit;

    /**
     * Tax percentage (optional)
     */
    private Integer taxPercentage;
}
