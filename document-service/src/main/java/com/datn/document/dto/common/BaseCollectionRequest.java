package com.datn.document.dto.common;

import lombok.Data;

@Data
public abstract class BaseCollectionRequest {
    private int page = 1;
    private int pageSize = 10;
    private String sort = "asc";
    public String getValidatedSort() {
        if ("desc".equalsIgnoreCase(sort)) {
            return "desc";
        }
        return "asc";
    }
}