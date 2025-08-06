package com.datn.document.dto.request;

import com.datn.document.dto.common.BaseCollectionRequest;
import lombok.*;

@Data
@EqualsAndHashCode(callSuper = true)
public class PresentationCollectionRequest extends BaseCollectionRequest {
    
    private String filter;

    @Builder
    public PresentationCollectionRequest(int page, int pageSize, String sort, String filter) {
        super(page, pageSize, sort);
        this.filter = filter;
    }
}