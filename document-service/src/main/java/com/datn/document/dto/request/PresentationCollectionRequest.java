package com.datn.document.dto.request;

import com.datn.document.dto.common.BaseCollectionRequest;
import lombok.*;
import jakarta.validation.constraints.Size;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PresentationCollectionRequest extends BaseCollectionRequest {

    @Size(max = 100, message = "Filter length cannot exceed 100 characters")
    private String filter;

    @Builder
    public PresentationCollectionRequest(int page, int pageSize, String sort, String filter) {
        super(page, pageSize, sort);
        this.filter = filter;
    }
}