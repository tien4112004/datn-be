package com.datn.datnbe.document.exam.api;

import com.datn.datnbe.document.exam.dto.ExamMatrixDto;
import com.datn.datnbe.document.exam.dto.request.GenerateExamFromMatrixRequest;
import com.datn.datnbe.document.exam.dto.request.GenerateMatrixRequest;
import com.datn.datnbe.document.exam.dto.response.ExamDraftDto;

public interface ExamApi {

    /**
    * Generate an exam matrix using AI.
    * The matrix has dimensions: [topic][difficulty][question_type]
    *
    * @param request   The request containing matrix generation parameters
    * @param teacherId The ID of the teacher creating the matrix
    * @return Generated exam matrix
    */
    ExamMatrixDto generateMatrix(GenerateMatrixRequest request, String teacherId);

    /**
     * Generate an exam by selecting questions from the question bank
     * based on the provided matrix.
     *
     * @param request   The request containing the matrix and configuration
     * @param teacherId The ID of the teacher (for personal questions)
     * @return ExamDraftDto containing selected questions and any gaps
     */
    ExamDraftDto generateExamFromMatrix(GenerateExamFromMatrixRequest request, String teacherId);
}
