package com.datn.datnbe.document.management;

import com.datn.datnbe.ai.api.CoinPricingApi;
import com.datn.datnbe.ai.api.ContentGenerationApi;
import com.datn.datnbe.ai.api.TokenUsageApi;
import com.datn.datnbe.ai.dto.response.TokenUsageInfoDto;
import com.datn.datnbe.ai.entity.TokenUsage;
import com.datn.datnbe.ai.service.PhoenixQueryService;
import com.datn.datnbe.auth.api.ResourcePermissionApi;
import com.datn.datnbe.auth.dto.request.ResourceRegistrationRequest;
import com.datn.datnbe.document.api.AssignmentApi;
import com.datn.datnbe.document.dto.DocumentMetadataDto;
import com.datn.datnbe.document.dto.AssignmentMatrixDto;
import com.datn.datnbe.document.dto.request.AssignmentCreateRequest;
import com.datn.datnbe.document.dto.request.AssignmentSettingsUpdateRequest;
import com.datn.datnbe.document.dto.request.AssignmentUpdateRequest;
import com.datn.datnbe.document.dto.request.GenerateAssignmentFromMatrixRequest;
import com.datn.datnbe.document.dto.request.GenerateMatrixRequest;
import com.datn.datnbe.document.dto.response.AssignmentResponse;
import com.datn.datnbe.document.dto.response.AssignmentDraftDto;
import com.datn.datnbe.document.entity.AssignmentMatrixEntity;
import com.datn.datnbe.document.repository.AssignmentMatrixTemplateRepository;
import com.datn.datnbe.document.service.DocumentService;
import com.datn.datnbe.document.service.QuestionSelectionService;

import com.datn.datnbe.document.entity.Assignment;
import com.datn.datnbe.document.dto.request.QuestionItemRequest;
import com.datn.datnbe.document.entity.Question;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.datn.datnbe.document.mapper.AssignmentMapper;
import com.datn.datnbe.document.repository.AssignmentRepository;

import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginationDto;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import com.datn.datnbe.sharedkernel.security.utils.SecurityContextUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssignmentManagement implements AssignmentApi {

    private final AssignmentRepository assignmentRepository;
    private final AssignmentMapper assignmentMapper;
    private final SecurityContextUtils securityContextUtils;
    private final ResourcePermissionApi resourcePermissionApi;
    private final DocumentService documentVisitService;
    private final ContentGenerationApi contentGenerationApi;
    private final QuestionSelectionService questionSelectionService;
    private final AssignmentMatrixTemplateRepository assignmentMatrixRepository;
    private final ObjectMapper objectMapper;
    private final TokenUsageApi tokenUsageApi;
    private final PhoenixQueryService phoenixQueryService;
    private final CoinPricingApi coinPricingApi;

    @Override
    @Transactional
    public AssignmentResponse createAssignment(AssignmentCreateRequest request) {
        String userId = securityContextUtils.getCurrentUserId();
        Assignment assignment = assignmentMapper.toEntity(request);
        assignment.setOwnerId(userId);

        if (request.getQuestions() != null && !request.getQuestions().isEmpty()) {
            assignment.setQuestions(mapQuestionItems(request.getQuestions()));
        }

        Assignment saved = assignmentRepository.save(assignment);

        // Register resource
        ResourceRegistrationRequest resourceRequest = ResourceRegistrationRequest.builder()
                .id(saved.getId())
                .name(saved.getTitle())
                .resourceType("assignment")
                .build();
        resourcePermissionApi.registerResource(resourceRequest, userId);

        return assignmentMapper.toDto(saved);
    }

    @Override
    public PaginatedResponseDto<AssignmentResponse> getAssignments(int page, int size, String search) {
        String userId = securityContextUtils.getCurrentUserId();
        List<String> allowedIds = resourcePermissionApi.getAllResourceByTypeOfOwner(userId, "assignment");

        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size, Sort.by("createdAt").descending());

        Page<Assignment> assignmentPage = assignmentRepository.findByIdIn(allowedIds, pageable);

        return PaginatedResponseDto.<AssignmentResponse>builder()
                .data(assignmentPage.getContent().stream().map(assignmentMapper::toDto).collect(Collectors.toList()))
                .pagination(PaginationDto.builder()
                        .currentPage(assignmentPage.getNumber() + 1)
                        .pageSize(assignmentPage.getSize())
                        .totalItems(assignmentPage.getTotalElements())
                        .totalPages(assignmentPage.getTotalPages())
                        .build())
                .build();
    }

    @Override
    public AssignmentResponse getAssignmentById(String id) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Assignment not found"));

        String userId = securityContextUtils.getCurrentUserId();
        if (userId != null) {
            DocumentMetadataDto metadata = DocumentMetadataDto.builder()
                    .userId(userId)
                    .documentId(id)
                    .type("assignment")
                    .title(assignment.getTitle())
                    .thumbnail(null)
                    .build();
            documentVisitService.trackDocumentVisit(metadata);
        }

        return assignmentMapper.toDto(assignment);
    }

    @Override
    @Transactional
    public AssignmentResponse updateAssignment(String id, AssignmentUpdateRequest request) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Assignment not found"));

        assignmentMapper.updateEntity(assignment, request);

        if (request.getQuestions() != null) {
            assignment.setQuestions(mapQuestionItems(request.getQuestions()));
        }

        Assignment saved = assignmentRepository.save(assignment);
        return assignmentMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteAssignment(String id) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Assignment not found"));
        assignmentRepository.delete(assignment);
    }

    @Override
    @Transactional
    public AssignmentResponse updateAssignmentSettings(String id, AssignmentSettingsUpdateRequest request) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Assignment not found"));

        String currentUserId = securityContextUtils.getCurrentUserId();
        if (!currentUserId.equals(assignment.getOwnerId())) {
            throw new AppException(ErrorCode.FORBIDDEN, "You don't have permission to update this assignment");
        }

        if (request.getTopics() != null) {
            assignment.setTopics(request.getTopics());
        }
        if (request.getMatrixCells() != null) {
            assignment.setMatrixCells(request.getMatrixCells());
        }

        Assignment saved = assignmentRepository.save(assignment);
        return assignmentMapper.toDto(saved);
    }

    private List<Question> mapQuestionItems(List<QuestionItemRequest> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }

        return items.stream()
                .map(item -> Question.builder()
                        .id(item.getId())
                        .type(item.getType())
                        .difficulty(item.getDifficulty())
                        .title(item.getTitle())
                        .titleImageUrl(item.getTitleImageUrl())
                        .explanation(item.getExplanation())
                        .grade(item.getGrade())
                        .chapter(item.getChapter())
                        .subject(item.getSubject())
                        .contextId(item.getContextId())
                        .topicId(item.getTopicId())
                        .data(item.getData())
                        .point(item.getPoint())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public AssignmentMatrixDto generateMatrix(GenerateMatrixRequest request, String teacherId) {
        log.info("Generating exam matrix for grade: {}, subject: {} by teacher: {}",
                request.getGrade(),
                request.getSubject(),
                teacherId);
        String matrixId = UUID.randomUUID().toString();
        AssignmentMatrixDto matrixDto = contentGenerationApi.generateAssignmentMatrix(request, matrixId);
        extractAndSaveTokenUsage(matrixId, request, "matrix", teacherId);

        // Persist the generated matrix
        log.info("Persisting generated matrix with ID: {}", matrixId);

        try {
            String matrixJson = objectMapper.writeValueAsString(matrixDto);
            AssignmentMatrixEntity examMatrix = AssignmentMatrixEntity.builder()
                    .id(matrixId)
                    .ownerId(teacherId != null ? teacherId.toString() : "system")
                    .name(null) // Set to null since matrix is assigned with an assignment
                    .subject(matrixDto.getMetadata() != null ? matrixDto.getMetadata().getSubject() : null)
                    .grade(matrixDto.getMetadata() != null ? matrixDto.getMetadata().getGrade() : null)
                    .matrixData(matrixJson)
                    .build();

            assignmentMatrixRepository.save(examMatrix);
            log.info("Matrix persisted successfully with ID: {}", matrixId);
        } catch (Exception e) {
            log.error("Failed to persist matrix: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to persist exam matrix", e);
        }

        return matrixDto;
    }

    @Override
    public AssignmentDraftDto generateAssignmentFromMatrix(GenerateAssignmentFromMatrixRequest request,
            String teacherId) {
        log.info("Generating exam from matrix for teacher: {}", teacherId);

        // Validate that either matrixId or matrix is provided
        if (request.getMatrixId() == null && request.getMatrix() == null) {
            throw new IllegalArgumentException("Either matrixId or matrix must be provided");
        }

        AssignmentMatrixDto matrix;

        // If matrixId is provided, load from database
        if (request.getMatrixId() != null) {
            log.info("Loading matrix from database: {}", request.getMatrixId());
            AssignmentMatrixEntity savedMatrix = assignmentMatrixRepository
                    .findByIdAndOwnerId(request.getMatrixId().toString(),
                            teacherId != null ? teacherId.toString() : null)
                    .orElseThrow(
                            () -> new IllegalArgumentException("Matrix not found with id: " + request.getMatrixId()));

            try {
                matrix = objectMapper.readValue(savedMatrix.getMatrixData(), AssignmentMatrixDto.class);
                log.info("Loaded matrix for subject: {}", matrix.getMetadata().getSubject());
            } catch (Exception e) {
                log.error("Failed to deserialize matrix data: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to read exam matrix from database", e);
            }
        } else {
            matrix = request.getMatrix();
        }

        // Use the QuestionSelectionService to select questions from the question bank
        AssignmentDraftDto draft = questionSelectionService
                .selectQuestionsForMatrix(
                        GenerateAssignmentFromMatrixRequest.builder()
                                .subject(request.getSubject())
                                .title(request.getTitle())
                                .description(request.getDescription())
                                .matrix(matrix)
                                .timeLimitMinutes(request.getTimeLimitMinutes())
                                .missingStrategy(request.getMissingStrategy())
                                .includePersonalQuestions(request.getIncludePersonalQuestions())
                                .build(),
                        teacherId);

        // Persist the matrix with the same ID as the generated draft
        // This allows joining assignment and exam_matrices tables
        if (request.getMatrixId() == null && draft.getId() != null) {
            log.info("Persisting exam matrix with ID: {}", draft.getId());
            try {
                String matrixJson = objectMapper.writeValueAsString(matrix);
                AssignmentMatrixEntity examMatrix = AssignmentMatrixEntity.builder()
                        .id(draft.getId())
                        .ownerId(teacherId != null ? teacherId.toString() : draft.getOwnerId())
                        .name(null) // Set to null since matrix is assigned with an assignment
                        .subject(matrix.getMetadata() != null ? matrix.getMetadata().getSubject() : null)
                        .grade(matrix.getMetadata() != null ? matrix.getMetadata().getGrade() : null)
                        .matrixData(matrixJson)
                        .build();
                assignmentMatrixRepository.save(examMatrix);
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
