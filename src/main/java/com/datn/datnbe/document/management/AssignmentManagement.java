package com.datn.datnbe.document.management;

import com.datn.datnbe.ai.api.CoinPricingApi;
import com.datn.datnbe.ai.api.ContentGenerationApi;
import com.datn.datnbe.ai.api.TokenUsageApi;
import com.datn.datnbe.ai.dto.request.GenerateQuestionsFromMatrixRequest;
import com.datn.datnbe.ai.dto.request.GenerateQuestionsFromMatrixRequest.ContextInfo;
import com.datn.datnbe.ai.dto.request.GenerateQuestionsFromMatrixRequest.TopicRequirement;
import com.datn.datnbe.ai.dto.request.GenerateQuestionsFromMatrixRequest.QuestionRequirement;
import com.datn.datnbe.ai.dto.response.GenerateQuestionsFromMatrixResponse;
import com.datn.datnbe.ai.dto.response.TokenUsageInfoDto;
import com.datn.datnbe.ai.entity.TokenUsage;
import com.datn.datnbe.ai.service.PhoenixQueryService;
import com.datn.datnbe.auth.api.ResourcePermissionApi;
import com.datn.datnbe.auth.dto.request.ResourceRegistrationRequest;
import com.datn.datnbe.document.api.AssignmentApi;
import com.datn.datnbe.document.dto.DimensionTopicDto;
import com.datn.datnbe.document.dto.DocumentMetadataDto;
import com.datn.datnbe.document.dto.AssignmentMatrixDto;
import com.datn.datnbe.document.dto.request.AssignmentCreateRequest;
import com.datn.datnbe.document.dto.request.AssignmentSettingsUpdateRequest;
import com.datn.datnbe.document.dto.request.AssignmentUpdateRequest;
import com.datn.datnbe.document.dto.request.GenerateAssignmentFromMatrixRequest;
import com.datn.datnbe.document.dto.request.GenerateFullAssignmentRequest;
import com.datn.datnbe.document.dto.request.GenerateMatrixRequest;
import com.datn.datnbe.document.dto.response.AssignmentResponse;
import com.datn.datnbe.document.dto.response.AssignmentDraftDto;
import com.datn.datnbe.document.entity.AssignmentMatrixEntity;
import com.datn.datnbe.document.repository.AssignmentMatrixTemplateRepository;
import com.datn.datnbe.document.service.DocumentService;
import com.datn.datnbe.document.service.QuestionSelectionService;

import com.datn.datnbe.document.entity.Assignment;
import com.datn.datnbe.document.entity.Context;
import com.datn.datnbe.document.dto.request.QuestionItemRequest;
import com.datn.datnbe.document.entity.Question;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.datn.datnbe.document.mapper.AssignmentMapper;
import com.datn.datnbe.document.repository.AssignmentRepository;
import com.datn.datnbe.document.repository.ContextRepository;

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

import java.util.*;
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
    private final ContextRepository contextRepository;

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

    @Override
    public AssignmentDraftDto generateFullAssignment(GenerateFullAssignmentRequest request, String teacherId) {
        log.info("Generating full assignment with AI for teacher: {}", teacherId);

        // 1. Load or use matrix
        AssignmentMatrixDto matrix = loadOrUseMatrix(request.getMatrixId(), request.getMatrix());
        String grade = matrix.getMetadata().getGrade();
        String subject = matrix.getMetadata().getSubject();

        log.info("Matrix loaded: grade={}, subject={}", grade, subject);

        // 2. Identify context-based topics and select random contexts
        Map<Integer, Context> selectedContexts = selectRandomContextsForMatrix(matrix, grade, subject);

        log.info("Selected {} contexts for context-based topics", selectedContexts.size());

        // 3. Build topic requirements with grouped question specifications
        List<TopicRequirement> topicRequirements = buildTopicRequirements(matrix, selectedContexts);

        log.info("Built requirements for {} topics", topicRequirements.size());

        // 4. Call GenAI Gateway to generate questions
        String traceId = UUID.randomUUID().toString();
        GenerateQuestionsFromMatrixRequest genAiRequest = GenerateQuestionsFromMatrixRequest.builder()
                .grade(grade)
                .subject(subject)
                .topics(topicRequirements)
                .provider(request.getProvider() != null ? request.getProvider() : "google")
                .model(request.getModel() != null ? request.getModel() : "gemini-2.5-flash")
                .build();

        GenerateQuestionsFromMatrixResponse genAiResponse = contentGenerationApi
                .generateQuestionsFromMatrix(genAiRequest, traceId);

        log.info("Received raw JSON response from GenAI Gateway");

        // 5. Parse raw JSON and enrich with common fields
        List<Question> questions = parseAndEnrichQuestions(genAiResponse
                .getRawJson(), grade, subject, selectedContexts, matrix.getDimensions().getTopics());

        log.info("Parsed and enriched {} questions", questions.size());

        // 6. Build and return draft
        return buildDraftFromGeneratedQuestions(request
                .getTitle(), request.getDescription(), request.getTimeLimitMinutes(), questions, teacherId, matrix);
    }

    private AssignmentMatrixDto loadOrUseMatrix(String matrixId, AssignmentMatrixDto matrix) {
        if (matrixId != null) {
            log.info("Loading matrix from database: {}", matrixId);
            AssignmentMatrixEntity savedMatrix = assignmentMatrixRepository.findById(matrixId)
                    .orElseThrow(() -> new IllegalArgumentException("Matrix not found with id: " + matrixId));

            try {
                return objectMapper.readValue(savedMatrix.getMatrixData(), AssignmentMatrixDto.class);
            } catch (Exception e) {
                log.error("Failed to deserialize matrix data: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to read matrix from database", e);
            }
        } else if (matrix != null) {
            return matrix;
        } else {
            throw new IllegalArgumentException("Either matrixId or matrix must be provided");
        }
    }

    private Map<Integer, Context> selectRandomContextsForMatrix(AssignmentMatrixDto matrix,
            String grade,
            String subject) {
        Map<Integer, Context> result = new HashMap<>();

        // Get all topics from dimensions
        List<DimensionTopicDto> topics = matrix.getDimensions().getTopics();

        for (int i = 0; i < topics.size(); i++) {
            DimensionTopicDto topic = topics.get(i);
            if (Boolean.TRUE.equals(topic.getHasContext())) {
                // Randomly select a context for this topic
                List<Context> availableContexts = contextRepository.findByGradeAndSubject(grade, subject);

                if (availableContexts.isEmpty()) {
                    throw new IllegalStateException(
                            String.format("No contexts available for grade=%s, subject=%s", grade, subject));
                }

                // Random selection
                Context randomContext = availableContexts.get(new Random().nextInt(availableContexts.size()));
                result.put(i, randomContext);

                log.info("Selected context '{}' for topic '{}' (index {})",
                        randomContext.getTitle(),
                        topic.getName(),
                        i);
            }
        }

        return result;
    }

    /**
     * Build topic requirements with grouped question specifications.
     * Structure: topics -> difficulty -> question_type -> {count, points}
     */
    private List<TopicRequirement> buildTopicRequirements(AssignmentMatrixDto matrix,
            Map<Integer, Context> selectedContexts) {
        List<TopicRequirement> topicRequirements = new ArrayList<>();

        List<DimensionTopicDto> topics = matrix.getDimensions().getTopics();
        List<String> difficulties = matrix.getDimensions().getDifficulties();
        List<String> questionTypes = matrix.getDimensions().getQuestionTypes();

        int topicIndex = 0;
        for (DimensionTopicDto topic : topics) {
            // Build questionsPerDifficulty map for this topic
            Map<String, Map<String, QuestionRequirement>> questionsPerDifficulty = new HashMap<>();

            for (int diffIndex = 0; diffIndex < difficulties.size(); diffIndex++) {
                String difficulty = difficulties.get(diffIndex);
                Map<String, QuestionRequirement> questionsPerType = new HashMap<>();

                for (int qtIndex = 0; qtIndex < questionTypes.size(); qtIndex++) {
                    String questionType = questionTypes.get(qtIndex);

                    // Get cell value (format: "count:points")
                    String cellValue = matrix.getMatrix().get(topicIndex).get(diffIndex).get(qtIndex);
                    String[] parts = cellValue.split(":");
                    int count = Integer.parseInt(parts[0]);
                    double points = Double.parseDouble(parts[1]);

                    if (count > 0) {
                        QuestionRequirement requirement = QuestionRequirement.builder()
                                .count(count)
                                .points(points)
                                .build();

                        questionsPerType.put(questionType, requirement);
                    }
                }

                if (!questionsPerType.isEmpty()) {
                    questionsPerDifficulty.put(difficulty, questionsPerType);
                }
            }

            // Only add topic if it has any questions
            if (!questionsPerDifficulty.isEmpty()) {
                // Check if this topic has a context
                Context context = selectedContexts.get(topicIndex);
                ContextInfo contextInfo = null;

                if (context != null) {
                    contextInfo = ContextInfo.builder()
                            .topicIndex(topicIndex)
                            .topicName(topic.getName())
                            .contextId(context.getId())
                            .contextType("TEXT") // Assuming text for now
                            .contextContent(context.getContent())
                            .contextTitle(context.getTitle())
                            .build();

                    log.debug("Topic '{}' (index {}) has context: {} (ID: {})",
                            topic.getName(),
                            topicIndex,
                            context.getTitle(),
                            context.getId());
                }

                TopicRequirement topicReq = TopicRequirement.builder()
                        .topicIndex(topicIndex)
                        .topicName(topic.getName())
                        .contextInfo(contextInfo)
                        .questionsPerDifficulty(questionsPerDifficulty)
                        .build();

                topicRequirements.add(topicReq);
            }

            topicIndex++;
        }

        return topicRequirements;
    }

    /**
     * Parse raw JSON response from GenAI Gateway and enrich with common fields.
     * Fills in: grade, subject, chapter, contextId based on topicId.
     */
    private List<Question> parseAndEnrichQuestions(String rawJson,
            String grade,
            String subject,
            Map<Integer, Context> selectedContexts,
            List<DimensionTopicDto> topics) {

        try {
            // Parse raw JSON
            com.fasterxml.jackson.databind.JsonNode rootNode = objectMapper.readTree(rawJson);
            com.fasterxml.jackson.databind.JsonNode questionsNode = rootNode.get("questions");

            if (questionsNode == null || !questionsNode.isArray()) {
                throw new IllegalArgumentException("Invalid response: missing 'questions' array");
            }

            // Build topic index to name mapping
            Map<Integer, String> topicIndexToName = new HashMap<>();
            for (int i = 0; i < topics.size(); i++) {
                topicIndexToName.put(i, topics.get(i).getName());
            }

            // Build topic index to context ID mapping
            Map<Integer, String> topicIndexToContextId = new HashMap<>();
            for (Map.Entry<Integer, Context> entry : selectedContexts.entrySet()) {
                topicIndexToContextId.put(entry.getKey(), entry.getValue().getId());
            }

            // Parse and enrich each question
            List<Question> enrichedQuestions = new ArrayList<>();
            for (com.fasterxml.jackson.databind.JsonNode questionNode : questionsNode) {
                // Convert to mutable ObjectNode for modification
                com.fasterxml.jackson.databind.node.ObjectNode questionObj = (com.fasterxml.jackson.databind.node.ObjectNode) questionNode;

                // Fill in common fields
                questionObj.put("grade", grade);
                questionObj.put("subject", subject);

                // Get topicId and set chapter and contextId
                Integer topicId = questionNode.has("topicId") ? questionNode.get("topicId").asInt() : null;
                if (topicId != null) {
                    // Set chapter from topic name
                    String topicName = topicIndexToName.get(topicId);
                    if (topicName != null) {
                        questionObj.put("chapter", topicName);
                    }

                    // Set contextId if this topic has a context
                    String contextId = topicIndexToContextId.get(topicId);
                    if (contextId != null) {
                        questionObj.put("contextId", contextId);
                        log.debug("Question for topic {} linked to context {}", topicId, contextId);
                    }
                }

                // Parse to Question entity from the enriched questionObj (not original questionNode)
                Question question = objectMapper.treeToValue(questionObj, Question.class);

                // Generate ID for the question
                if (question.getId() == null) {
                    question.setId(UUID.randomUUID().toString());
                }

                enrichedQuestions.add(question);
            }

            log.info("Successfully parsed and enriched {} questions", enrichedQuestions.size());
            return enrichedQuestions;

        } catch (Exception e) {
            log.error("Failed to parse raw JSON response: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse GenAI response", e);
        }
    }

    private AssignmentDraftDto buildDraftFromGeneratedQuestions(String title,
            String description,
            Integer timeLimitMinutes,
            List<Question> questions,
            String teacherId,
            AssignmentMatrixDto matrix) {
        // Create a draft assignment
        String draftId = UUID.randomUUID().toString();

        // Calculate totals
        double totalPoints = questions.stream().mapToDouble(q -> q.getPoint() != null ? q.getPoint() : 1.0).sum();

        // Build the draft
        return AssignmentDraftDto.builder()
                .id(draftId)
                .title(title)
                .description(description)
                .duration(timeLimitMinutes)
                .questions(questions)
                .totalQuestions(questions.size())
                .totalPoints(totalPoints)
                .isComplete(true)
                .ownerId(teacherId)
                .subject(matrix.getMetadata() != null ? matrix.getMetadata().getSubject() : null)
                .grade(matrix.getMetadata() != null ? matrix.getMetadata().getGrade() : null)
                .build();
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
