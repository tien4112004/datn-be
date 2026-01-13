package com.datn.datnbe.document.dto;

import lombok.*;

/**
 * DTO for document metadata to be tracked
 * Contains all information needed to record a document visit
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentMetadataDto {
    private String userId;
    private String documentId;
    private String type;
    private String title;
    private String thumbnail;
}
