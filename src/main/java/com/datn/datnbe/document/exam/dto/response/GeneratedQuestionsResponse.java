package com.datn.datnbe.document.exam.dto.response;

import com.datn.datnbe.document.dto.response.QuestionResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedQuestionsResponse {
    private List<String> questionIds;
    private Integer totalGenerated;
    private String message;
    private List<QuestionResponseDto> questions;
}
