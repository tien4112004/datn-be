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
}
