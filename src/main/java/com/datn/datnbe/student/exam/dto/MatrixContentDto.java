package com.datn.datnbe.student.exam.dto;

import com.datn.datnbe.student.exam.enums.ExamDifficulty;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatrixContentDto {
    private ExamDifficulty difficulty;
    
    @JsonProperty("numberOfQuestions")
    @JsonAlias("number_of_questions")
    private Integer numberOfQuestions;
    
    @JsonProperty("selectedQuestions")
    @JsonAlias("selected_questions")
    private List<String> selectedQuestions;
}
