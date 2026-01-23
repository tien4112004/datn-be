package com.datn.datnbe.student.exam.service;

import com.datn.datnbe.student.exam.api.ExamApi;
import com.datn.datnbe.student.exam.apiclient.ExamGenerationApiClient;
import com.datn.datnbe.student.exam.dto.ExamMatrixDto;
import com.datn.datnbe.student.exam.dto.ExamMatrixV2Dto;
import com.datn.datnbe.student.exam.dto.request.GenerateExamFromMatrixRequest;
import com.datn.datnbe.student.exam.dto.request.GenerateMatrixRequest;
import com.datn.datnbe.student.exam.dto.request.GenerateMatrixV2Request;
import com.datn.datnbe.student.exam.dto.response.ExamDraftDto;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class ExamService implements ExamApi {

    ExamGenerationApiClient examGenerationApiClient;
    QuestionSelectionService questionSelectionService;

    @Override
    public ExamMatrixDto generateMatrix(GenerateMatrixRequest request) {
        log.info("Generating exam matrix for topic: {}", request.getTopic());
        return examGenerationApiClient.generateMatrix(request);
    }

    @Override
    public ExamMatrixV2Dto generateMatrixV2(GenerateMatrixV2Request request) {
        log.info("Generating V2 exam matrix for topics: {}", request.getTopics());
        return examGenerationApiClient.generateMatrixV2(request);
    }

    @Override
    public ExamDraftDto generateExamFromMatrix(GenerateExamFromMatrixRequest request, UUID teacherId) {
        log.info("Generating exam from matrix for teacher: {}", teacherId);

        // Validate that either matrixId or matrix is provided
        if (request.getMatrixId() == null && request.getMatrix() == null) {
            throw new IllegalArgumentException("Either matrixId or matrix must be provided");
        }

        // If matrixId is provided, we would need to fetch it from the database
        // For now, we only support inline matrix
        if (request.getMatrix() == null) {
            throw new UnsupportedOperationException(
                    "Loading matrix by ID is not yet implemented. Please provide the matrix inline.");
        }

        // Use the QuestionSelectionService to select questions from the question bank
        ExamDraftDto draft = questionSelectionService.selectQuestionsForMatrix(request, teacherId);

        log.info("Generated exam draft with {} questions ({} complete)",
                draft.getTotalQuestions(),
                draft.getIsComplete() ? "complete" : "has gaps");

        return draft;
    }
}
