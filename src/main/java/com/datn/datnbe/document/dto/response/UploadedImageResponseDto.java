package com.datn.datnbe.document.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadedImageResponseDto {
    private String imageUrl;
}
