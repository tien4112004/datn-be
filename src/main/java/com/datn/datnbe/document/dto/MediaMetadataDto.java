package com.datn.datnbe.document.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaMetadataDto {
    private Boolean isGenerated;
    private String presentationId;
    private String prompt;
    private String model;
    private String provider;
}
