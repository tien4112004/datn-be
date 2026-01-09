package com.datn.datnbe.document.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecentDocumentDto {
    private String id;
    private String documentId;
    private String documentType;
    private String documentTitle;
    private String thumbnail;
    private LocalDateTime lastVisited;
    private Integer visitCount;
}
