package com.datn.datnbe.cms.dto.request;

import java.util.List;

import com.datn.datnbe.cms.dto.AnswerDataDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SubmissionCreateRequest {
    private String studentId;
    private String postId;
    private List<AnswerDataDto> questions;
}
