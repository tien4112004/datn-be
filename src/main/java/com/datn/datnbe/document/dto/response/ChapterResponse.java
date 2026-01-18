package com.datn.datnbe.document.dto.response;

import com.datn.datnbe.document.entity.Chapter;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChapterResponse {
    private String id;
    private String name;
    private String grade;
    private String subject;
    private String sortOrder;

    public static ChapterResponse fromChapter(Chapter chapter) {
        return ChapterResponse.builder()
                .id(chapter.getId())
                .name(chapter.getName())
                .grade(chapter.getGrade())
                .subject(chapter.getSubject())
                .sortOrder(chapter.getSortOrder())
                .build();
    }
}
