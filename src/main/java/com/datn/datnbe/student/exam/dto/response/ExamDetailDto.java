package com.datn.datnbe.student.exam.dto.response;

import com.datn.datnbe.student.exam.entity.Exam;
import com.datn.datnbe.student.exam.enums.ExamDifficulty;
import com.datn.datnbe.student.exam.enums.ExamStatus;
import com.datn.datnbe.student.exam.enums.GradeLevel;
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
public class ExamDetailDto {
    private UUID examId;
    private UUID teacherId;
    private String title;
    private String description;
    private String topic;
    private GradeLevel gradeLevel;
    private ExamDifficulty difficulty;
    private ExamStatus status;
    private Integer totalQuestions;
    private Integer totalPoints;
    private Integer timeLimitMinutes;
    private Exam.QuestionOrder questionOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
