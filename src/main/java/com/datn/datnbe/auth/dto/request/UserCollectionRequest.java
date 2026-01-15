package com.datn.datnbe.auth.dto.request;

import com.datn.datnbe.sharedkernel.dto.BaseCollectionRequest;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class UserCollectionRequest extends BaseCollectionRequest {

    @Size(max = 100, message = "Search query cannot exceed 100 characters")
    private String search;

    @Builder
    public UserCollectionRequest(int page, int pageSize, String sort, String search) {
        super(page, pageSize, sort);
        this.search = search;
    }
}
