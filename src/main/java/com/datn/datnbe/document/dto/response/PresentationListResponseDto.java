package com.datn.datnbe.document.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class PresentationListResponseDto {
    private String id;
    private String title;
    private String thumbnail;
    private String grade;
    private String subject;
    private String chapter;
    private String chapterId;
    private Date createdAt;
    private Date updatedAt;
}
