package com.datn.datnbe.student.exam.api;

import com.datn.datnbe.student.exam.dto.ExamMatrixDto;
import com.datn.datnbe.student.exam.dto.ExamMatrixV2Dto;
import com.datn.datnbe.student.exam.dto.request.GenerateExamFromMatrixRequest;
import com.datn.datnbe.student.exam.dto.request.GenerateMatrixRequest;
import com.datn.datnbe.student.exam.dto.request.GenerateMatrixV2Request;
import com.datn.datnbe.student.exam.dto.response.ExamDraftDto;

import java.util.UUID;

public interface ExamApi {

    /**
     * Generate an exam matrix using AI (legacy format).
     */
    ExamMatrixDto generateMatrix(GenerateMatrixRequest request);

    /**
     * Generate a 3D exam matrix using AI.
     * The matrix has dimensions: [topic][difficulty][question_type]
     */
    ExamMatrixV2Dto generateMatrixV2(GenerateMatrixV2Request request);

    /**
     * Generate an exam by selecting questions from the question bank
     * based on the provided matrix.
     *
     * @param request   The request containing the matrix and configuration
     * @param teacherId The ID of the teacher (for personal questions)
     * @return ExamDraftDto containing selected questions and any gaps
     */
    ExamDraftDto generateExamFromMatrix(GenerateExamFromMatrixRequest request, UUID teacherId);
}
