package com.datn.datnbe.document.dto.response;

import com.datn.datnbe.document.dto.SlideDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for detailed presentation information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PresentationDto {
    private String id;
    private String title;
    private List<SlideDto> slides;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isParsed;
}
