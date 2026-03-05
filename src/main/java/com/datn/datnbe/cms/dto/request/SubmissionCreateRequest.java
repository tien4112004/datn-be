package com.datn.datnbe.cms.dto.request;

import java.util.List;

import com.datn.datnbe.cms.dto.AnswerDataDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubmissionCreateRequest {
    private String postId;
    private List<AnswerDataDto> questions;
    @Builder.Default
    private boolean autoGrade = true;
}
