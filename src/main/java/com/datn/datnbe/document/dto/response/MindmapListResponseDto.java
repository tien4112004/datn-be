package com.datn.datnbe.document.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MindmapListResponseDto {
    private String id;
    private String title;
    private String description;
    //    private String ownerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
