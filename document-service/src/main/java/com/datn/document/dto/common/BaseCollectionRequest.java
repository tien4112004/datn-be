package com.datn.document.dto.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseCollectionRequest {
    protected int page = 1;
    protected int pageSize = 10;
    protected String sort = "asc";
    public String getValidatedSort() {
        if ("desc".equalsIgnoreCase(sort)) {
            return "desc";
        }
        return "asc";
    }
}