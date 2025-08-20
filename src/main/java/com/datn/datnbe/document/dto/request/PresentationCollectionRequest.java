package com.datn.datnbe.document.dto.request;

import com.datn.datnbe.sharedkernel.dto.BaseCollectionRequest;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

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