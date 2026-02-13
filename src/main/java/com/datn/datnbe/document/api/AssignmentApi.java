package com.datn.datnbe.document.api;

import com.datn.datnbe.document.dto.AssignmentMatrixDto;
import com.datn.datnbe.document.dto.request.AssignmentCreateRequest;
import com.datn.datnbe.document.dto.request.AssignmentSettingsUpdateRequest;
import com.datn.datnbe.document.dto.request.AssignmentUpdateRequest;
import com.datn.datnbe.document.dto.request.GenerateAssignmentFromMatrixRequest;
import com.datn.datnbe.document.dto.request.GenerateFullAssignmentRequest;
import com.datn.datnbe.document.dto.request.GenerateMatrixRequest;
import com.datn.datnbe.document.dto.response.AssignmentResponse;
import com.datn.datnbe.document.dto.response.AssignmentDraftDto;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;

public interface AssignmentApi {

    AssignmentResponse createAssignment(AssignmentCreateRequest request);

    PaginatedResponseDto<AssignmentResponse> getAssignments(int page, int size, String search);

    AssignmentResponse getAssignmentById(String id);

    AssignmentResponse updateAssignment(String id, AssignmentUpdateRequest request);

    void deleteAssignment(String id);

    AssignmentResponse updateAssignmentSettings(String id, AssignmentSettingsUpdateRequest request);

    /**
     * Generate an assignment matrix using AI.
     * The matrix has dimensions: [topic][difficulty][question_type]
     *
     * @param request   The request containing matrix generation parameters
     * @param teacherId The ID of the teacher creating the matrix
     * @return Generated assignment matrix
     */
    AssignmentMatrixDto generateMatrix(GenerateMatrixRequest request, String teacherId);

    /**
     * Generate an assignment by selecting questions from the question bank
     * based on the provided matrix.
     *
     * @param request   The request containing the matrix and configuration
     * @param teacherId The ID of the teacher (for personal questions)
     * @return AssignmentDraftDto containing selected questions and any gaps
     */
    AssignmentDraftDto generateAssignmentFromMatrix(GenerateAssignmentFromMatrixRequest request, String teacherId);

    /**
     * Generate a full assignment with AI-generated questions.
     * Supports both context-based and regular curriculum questions.
     *
     * Features:
     * - Topics with hasContext=true get random contexts from database
     * - All questions generated in single LLM call
     * - Questions automatically linked to contexts
     *
     * @param request   The request containing the matrix and assignment details
     * @param teacherId The ID of the teacher creating the assignment
     * @return AssignmentDraftDto containing the generated questions
     */
    AssignmentDraftDto generateFullAssignment(GenerateFullAssignmentRequest request, String teacherId);
}
