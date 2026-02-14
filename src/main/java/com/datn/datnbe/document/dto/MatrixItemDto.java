package com.datn.datnbe.document.dto;

import com.datn.datnbe.document.entity.questiondata.Difficulty;
import com.datn.datnbe.document.enums.ContextType;
import com.datn.datnbe.document.entity.questiondata.QuestionType;
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
    private Difficulty difficulty;
    private Boolean requiresContext;
    private ContextType contextType;
}
