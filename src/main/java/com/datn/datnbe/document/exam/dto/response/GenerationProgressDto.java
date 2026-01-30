package com.datn.datnbe.document.exam.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerationProgressDto {
    private Integer current;
    private Integer total;
    private String message;
}
