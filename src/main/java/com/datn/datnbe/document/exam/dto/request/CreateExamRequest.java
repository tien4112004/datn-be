package com.datn.datnbe.document.exam.dto.request;

import com.datn.datnbe.document.entity.questiondata.Difficulty;
import com.datn.datnbe.document.exam.enums.GradeLevel;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateExamRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    private String description;

    @NotBlank(message = "Topic is required")
    @Size(max = 255, message = "Topic must not exceed 255 characters")
    private String topic;

    @NotNull(message = "Grade level is required")
    private GradeLevel gradeLevel;

    @NotNull(message = "Difficulty is required")
    private Difficulty difficulty;

    @Min(value = 1, message = "Time limit must be at least 1 minute")
    private Integer timeLimitMinutes;
}
