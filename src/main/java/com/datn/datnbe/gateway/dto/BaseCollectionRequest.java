package com.datn.datnbe.gateway.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseCollectionRequest {
    public static final int DEFAULT_PAGE_SIZE = 10;

    @Min(value = 1, message = "Page must be at least 1")
    protected int page = 1;

    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size cannot exceed 100")
    protected int pageSize = DEFAULT_PAGE_SIZE;

    @Pattern(regexp = "asc|desc", flags = Pattern.Flag.CASE_INSENSITIVE, message = "Sort must be either 'asc' or 'desc'")
    protected String sort = "asc";

    public String getValidatedSort() {
        if ("desc".equalsIgnoreCase(sort)) {
            return "desc";
        }
        return "asc";
    }
}