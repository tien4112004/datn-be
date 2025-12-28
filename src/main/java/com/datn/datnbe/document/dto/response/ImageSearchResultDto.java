package com.datn.datnbe.document.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImageSearchResultDto {
    private Long id;
    private Integer width;
    private Integer height;
    private String src; // Main image URL
    private String photographer;
    private String photographerUrl;
    private String alt;
}
