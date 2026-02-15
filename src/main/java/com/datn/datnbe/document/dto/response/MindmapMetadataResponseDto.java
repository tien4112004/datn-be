package com.datn.datnbe.document.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MindmapMetadataResponseDto {
    private String mindmapId;
    private String title;
    private String description;
    private String rootNodeId;
    private String grade;
    private String subject;
}
