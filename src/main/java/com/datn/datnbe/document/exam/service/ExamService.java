package com.datn.datnbe.document.exam.service;

import com.datn.datnbe.ai.api.ContentGenerationApi;
import com.datn.datnbe.document.exam.api.ExamApi;
import com.datn.datnbe.document.exam.dto.ExamMatrixDto;
import com.datn.datnbe.document.exam.dto.request.GenerateExamFromMatrixRequest;
import com.datn.datnbe.document.exam.dto.request.GenerateMatrixRequest;
import com.datn.datnbe.document.exam.dto.response.ExamDraftDto;
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

    ContentGenerationApi contentGenerationApi;
    QuestionSelectionService questionSelectionService;

    @Override
    public ExamMatrixDto generateMatrix(GenerateMatrixRequest request) {
        log.info("Generating exam matrix for topics: {}", request.getTopics());
        return contentGenerationApi.generateExamMatrix(request);
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

        log.info("Generated exam draft with {} questions ({})",
                draft.getTotalQuestions(),
                draft.getIsComplete() ? "complete" : "has gaps");

        return draft;
    }
}
