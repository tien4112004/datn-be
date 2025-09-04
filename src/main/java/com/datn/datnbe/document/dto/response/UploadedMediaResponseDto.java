package com.datn.datnbe.document.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadedMediaResponseDto {
    private String cdnUrl;
    private String mediaType;
    private String extension;
}
