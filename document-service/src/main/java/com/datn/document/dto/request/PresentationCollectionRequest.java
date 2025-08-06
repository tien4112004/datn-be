package com.datn.document.dto.request;

import com.datn.document.dto.common.BaseCollectionRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PresentationCollectionRequest extends BaseCollectionRequest {
    
    private String filter;
}