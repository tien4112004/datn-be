package com.datn.datnbe.document.exam.service;

import com.datn.datnbe.ai.api.CoinPricingApi;
import com.datn.datnbe.ai.api.ContentGenerationApi;
import com.datn.datnbe.ai.api.TokenUsageApi;
import com.datn.datnbe.ai.dto.response.TokenUsageInfoDto;
import com.datn.datnbe.ai.entity.TokenUsage;
import com.datn.datnbe.ai.service.PhoenixQueryService;
import com.datn.datnbe.document.exam.api.ExamApi;
import com.datn.datnbe.document.exam.dto.ExamMatrixDto;
import com.datn.datnbe.document.exam.dto.request.GenerateExamFromMatrixRequest;
import com.datn.datnbe.document.exam.dto.request.GenerateMatrixRequest;
import com.datn.datnbe.document.exam.dto.response.ExamDraftDto;
import com.datn.datnbe.document.exam.entity.ExamMatrix;
import com.datn.datnbe.document.exam.repository.ExamMatrixRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class ExamService implements ExamApi {

    ContentGenerationApi contentGenerationApi;
    QuestionSelectionService questionSelectionService;
    ExamMatrixRepository examMatrixRepository;
    ObjectMapper objectMapper;
    TokenUsageApi tokenUsageApi;
    PhoenixQueryService phoenixQueryService;
    CoinPricingApi coinPricingApi;

    @Override
    public ExamMatrixDto generateMatrix(GenerateMatrixRequest request, String teacherId) {
        log.info("Generating exam matrix for topics: {} by teacher: {}", request.getTopics(), teacherId);
        String matrixId = UUID.randomUUID().toString();
        ExamMatrixDto matrixDto = contentGenerationApi.generateExamMatrix(request, matrixId);
        extractAndSaveTokenUsage(matrixId, request, "matrix", teacherId);

        // Persist the generated matrix
        log.info("Persisting generated matrix with ID: {}", matrixId);

        try {
            String matrixJson = objectMapper.writeValueAsString(matrixDto);
            ExamMatrix examMatrix = ExamMatrix.builder()
                    .id(matrixId)
                    .ownerId(teacherId != null ? teacherId.toString() : "system")
                    .name(matrixDto.getMetadata() != null ? matrixDto.getMetadata().getName() : null)
                    .subject(matrixDto.getMetadata() != null ? matrixDto.getMetadata().getSubject() : null)
                    .grade(matrixDto.getMetadata() != null ? matrixDto.getMetadata().getGrade() : null)
                    .matrixData(matrixJson)
                    .build();

            examMatrixRepository.save(examMatrix);
            log.info("Matrix persisted successfully with ID: {}", matrixId);
        } catch (Exception e) {
            log.error("Failed to persist matrix: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to persist exam matrix", e);
        }

        return matrixDto;
    }

    @Override
    public ExamDraftDto generateExamFromMatrix(GenerateExamFromMatrixRequest request, String teacherId) {
        log.info("Generating exam from matrix for teacher: {}", teacherId);

        // Validate that either matrixId or matrix is provided
        if (request.getMatrixId() == null && request.getMatrix() == null) {
            throw new IllegalArgumentException("Either matrixId or matrix must be provided");
        }

        ExamMatrixDto matrix;

        // If matrixId is provided, load from database
        if (request.getMatrixId() != null) {
            log.info("Loading matrix from database: {}", request.getMatrixId());
            ExamMatrix savedMatrix = examMatrixRepository
                    .findByIdAndOwnerId(request.getMatrixId().toString(),
                            teacherId != null ? teacherId.toString() : null)
                    .orElseThrow(
                            () -> new IllegalArgumentException("Matrix not found with id: " + request.getMatrixId()));

            try {
                matrix = objectMapper.readValue(savedMatrix.getMatrixData(), ExamMatrixDto.class);
                log.info("Loaded matrix for subject: {}", matrix.getMetadata().getSubject());
            } catch (Exception e) {
                log.error("Failed to deserialize matrix data: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to read exam matrix from database", e);
            }
        } else {
            matrix = request.getMatrix();
        }

        // Use the QuestionSelectionService to select questions from the question bank
        ExamDraftDto draft = questionSelectionService.selectQuestionsForMatrix(GenerateExamFromMatrixRequest.builder()
                .subject(request.getSubject())
                .title(request.getTitle())
                .description(request.getDescription())
                .matrix(matrix)
                .timeLimitMinutes(request.getTimeLimitMinutes())
                .missingStrategy(request.getMissingStrategy())
                .includePersonalQuestions(request.getIncludePersonalQuestions())
                .build(), teacherId);

        // Persist the matrix with the same ID as the generated draft
        // This allows joining assignment and exam_matrices tables
        if (request.getMatrixId() == null && draft.getId() != null) {
            log.info("Persisting exam matrix with ID: {}", draft.getId());
            try {
                String matrixJson = objectMapper.writeValueAsString(matrix);
                ExamMatrix examMatrix = ExamMatrix.builder()
                        .id(draft.getId())
                        .ownerId(teacherId != null ? teacherId.toString() : draft.getOwnerId())
                        .name(matrix.getMetadata() != null ? matrix.getMetadata().getName() : null)
                        .subject(matrix.getMetadata() != null ? matrix.getMetadata().getSubject() : null)
                        .grade(matrix.getMetadata() != null ? matrix.getMetadata().getGrade() : null)
                        .matrixData(matrixJson)
                        .build();
                examMatrixRepository.save(examMatrix);
                log.info("Exam matrix persisted successfully");
            } catch (Exception e) {
                log.error("Failed to persist matrix with draft ID: {}", e.getMessage(), e);
                // Don't fail the entire operation if matrix persistence fails
                // The draft is still valid and can be used
            }
        }

        log.info("Generated exam draft with {} questions ({})",
                draft.getTotalQuestions(),
                draft.getIsComplete() ? "complete" : "has gaps");

        return draft;
    }

    @Async
    protected void extractAndSaveTokenUsage(String traceId,
            GenerateMatrixRequest request,
            String requestType,
            String userId) {
        try {
            TokenUsageInfoDto tokenUsageInfo = phoenixQueryService.getTokenUsageFromPhoenix(traceId.replace("-", ""),
                    requestType);

            if (tokenUsageInfo != null && tokenUsageInfo.getTotalTokens() != null
                    && tokenUsageInfo.getTotalTokens() > 0) {
                tokenUsageInfo.setModel(request.getModel());
                tokenUsageInfo.setProvider(request.getProvider());
                Long totalTokens = tokenUsageInfo.getTotalTokens();

                Long PriceInCoinOfRequest = coinPricingApi.getTokenPriceInCoins(tokenUsageInfo.getModel(),
                        tokenUsageInfo.getProvider(),
                        requestType.toUpperCase());
                String requestBody = objectMapper.writeValueAsString(request);
                TokenUsage tokenUsage = TokenUsage.builder()
                        .userId(userId)
                        .request(requestType)
                        .inputTokens(tokenUsageInfo.getInputTokens())
                        .outputTokens(tokenUsageInfo.getOutputTokens())
                        .tokenCount(totalTokens)
                        .model(tokenUsageInfo.getModel())
                        .documentId(traceId)
                        .requestBody(requestBody)
                        .provider(tokenUsageInfo.getProvider())
                        .actualPrice(tokenUsageInfo.getTotalPrice())
                        .calculatedPrice(PriceInCoinOfRequest)
                        .build();
                tokenUsageApi.recordTokenUsage(tokenUsage);
                log.debug("Token usage saved with price: {}", tokenUsageInfo.getTotalPrice());
            } else {
                log.warn("No token usage data available from Phoenix for {} with traceId: {}", requestType, traceId);
            }
        } catch (Exception e) {
            log.warn("Failed to save token usage from Phoenix for generate-questions-from-topic", e);
        }
    }
}
