package com.datn.datnbe.cms.dto.request;

import com.datn.datnbe.sharedkernel.dto.BaseCollectionRequest;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClassCollectionRequest extends BaseCollectionRequest {

    @Size(max = 100, message = "Search query cannot exceed 100 characters")
    String search;

    Boolean isActive;

    @Builder
    public ClassCollectionRequest(int page, int pageSize, String sort, String search, Boolean isActive) {
        super(page, pageSize, sort);
        this.search = search;
        this.isActive = isActive;
    }
}
