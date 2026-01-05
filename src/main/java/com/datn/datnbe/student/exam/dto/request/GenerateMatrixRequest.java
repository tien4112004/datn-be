package com.datn.datnbe.student.exam.dto.request;

import com.datn.datnbe.student.exam.enums.ExamDifficulty;
import com.datn.datnbe.student.exam.enums.GradeLevel;
import com.datn.datnbe.student.exam.enums.QuestionType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateMatrixRequest {

    @NotBlank(message = "Topic is required")
    private String topic;

    @NotNull(message = "Grade level is required")
    private GradeLevel gradeLevel;

    @NotNull(message = "Difficulty is required")
    private ExamDifficulty difficulty;

    private String content;

    @NotNull(message = "Total questions is required")
    @Min(value = 1, message = "Total questions must be at least 1")
    private Integer totalQuestions;

    @NotNull(message = "Total points is required")
    @Min(value = 1, message = "Total points must be at least 1")
    private Integer totalPoints;

    private List<QuestionType> questionTypes;
}
