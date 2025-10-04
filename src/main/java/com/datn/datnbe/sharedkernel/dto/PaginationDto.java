package com.datn.datnbe.sharedkernel.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Pageable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaginationDto {
    private int currentPage;
    private int pageSize;
    private long totalItems;
    private int totalPages;

    public static PaginationDto getFromPageable(Pageable pageable) {
        return PaginationDto.builder().currentPage(pageable.getPageNumber()).pageSize(pageable.getPageSize()).build();
    }
}
