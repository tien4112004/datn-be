package com.datn.datnbe.ai.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AIGradeRequest {
    private String questionContent;
    private String questionExplaination;
    private String expectedAnswer;
    private String answerData;
    private String context;
    private String grade;
    private String subject;
    private String chapter;
    private Double maxScore;
}
