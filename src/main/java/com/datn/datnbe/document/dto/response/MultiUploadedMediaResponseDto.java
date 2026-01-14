package com.datn.datnbe.document.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MultiUploadedMediaResponseDto {
    private List<UploadedMediaResponseDto> media;
    private Integer totalCount;
}
