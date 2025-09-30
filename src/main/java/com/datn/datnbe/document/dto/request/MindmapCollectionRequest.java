package com.datn.datnbe.document.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Min;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MindmapCollectionRequest {
    @Min(value = 0, message = "Page must be non-negative")
    private Integer page = 0;

    @Min(value = 1, message = "Size must be at least 1")
    private Integer size = 10;

    private String sortBy = "createdAt";
    private String sortDirection = "desc";
    private String searchQuery;
}
