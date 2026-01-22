package com.datn.datnbe.document.dto.request;

import com.datn.datnbe.sharedkernel.dto.BaseCollectionRequest;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class RecentDocumentCollectionRequest extends BaseCollectionRequest {

    @Builder
    public RecentDocumentCollectionRequest(int page, int pageSize, String sort) {
        super(page, pageSize, sort);
    }
}
