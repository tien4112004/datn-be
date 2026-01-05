package com.datn.datnbe.student.exam.dto.response;

import com.datn.datnbe.student.exam.enums.ExamStatus;
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
public class ExamResponseDto {
    private UUID examId;
    private UUID teacherId;
    private String title;
    private ExamStatus status;
    private LocalDateTime createdAt;
}
