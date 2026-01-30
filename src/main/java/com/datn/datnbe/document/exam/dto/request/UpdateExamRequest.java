package com.datn.datnbe.document.exam.dto.request;

import com.datn.datnbe.document.exam.entity.Exam;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateExamRequest {

    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    private String description;

    @Min(value = 1, message = "Time limit must be at least 1 minute")
    private Integer timeLimitMinutes;

    private Exam.QuestionOrder questionOrder;
}
