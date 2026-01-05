package com.datn.datnbe.student.exam.api;

import com.datn.datnbe.student.exam.dto.ExamMatrixDto;
import com.datn.datnbe.student.exam.dto.request.CreateExamRequest;
import com.datn.datnbe.student.exam.dto.request.GenerateMatrixRequest;
import com.datn.datnbe.student.exam.dto.request.GenerateQuestionsRequest;
import com.datn.datnbe.student.exam.dto.request.UpdateExamRequest;
import com.datn.datnbe.student.exam.dto.response.ExamDetailDto;
import com.datn.datnbe.student.exam.dto.response.ExamResponseDto;
import com.datn.datnbe.student.exam.dto.response.ExamSummaryDto;
import com.datn.datnbe.student.exam.enums.ExamStatus;
import com.datn.datnbe.student.exam.enums.GradeLevel;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ExamApi {

    ExamResponseDto createExam(CreateExamRequest request, UUID teacherId);

    PaginatedResponseDto<ExamSummaryDto> getAllExams(UUID teacherId,
            Pageable pageable,
            ExamStatus status,
            String topic,
            GradeLevel gradeLevel);

    ExamDetailDto getExamById(UUID examId, UUID teacherId);

    ExamDetailDto updateExam(UUID examId, UpdateExamRequest request, UUID teacherId);

    void deleteExam(UUID examId, UUID teacherId);

    void archiveExam(UUID examId, UUID teacherId);

    ExamResponseDto duplicateExam(UUID examId, UUID teacherId);

    ExamMatrixDto generateMatrix(GenerateMatrixRequest request);

    String generateQuestions(UUID examId, GenerateQuestionsRequest request, UUID teacherId);
}
