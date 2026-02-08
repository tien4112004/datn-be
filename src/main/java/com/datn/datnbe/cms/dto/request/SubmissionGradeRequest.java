package com.datn.datnbe.cms.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SubmissionGradeRequest {
    // Map questionId -> score
    private Map<String, Integer> questionScores;

    // Map questionId -> feedback (per-question feedback)
    private Map<String, String> questionFeedback;

    // Overall feedback for the submission
    private String overallFeedback;
}
