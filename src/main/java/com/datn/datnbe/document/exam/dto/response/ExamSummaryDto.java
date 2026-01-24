package com.datn.datnbe.document.exam.dto.response;

import com.datn.datnbe.document.exam.enums.ExamStatus;
import com.datn.datnbe.document.exam.enums.GradeLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamSummaryDto {
    private UUID examId;
    private String title;
    private String topic;
    private GradeLevel gradeLevel;
    private ExamStatus status;
    private Integer totalQuestions;
    private LocalDateTime createdAt;
}
