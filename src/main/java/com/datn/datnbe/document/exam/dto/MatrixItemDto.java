package com.datn.datnbe.document.exam.dto;

import com.datn.datnbe.document.exam.enums.ContextType;
import com.datn.datnbe.document.exam.enums.ExamDifficulty;
import com.datn.datnbe.document.exam.enums.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatrixItemDto {
    private String topic;
    private QuestionType questionType;
    private Integer count;
    private Integer pointsEach;
    private ExamDifficulty difficulty;
    private Boolean requiresContext;
    private ContextType contextType;
}
