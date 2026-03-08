package com.datn.datnbe.document.service;

import com.datn.datnbe.ai.api.CoinPricingApi;
import com.datn.datnbe.ai.api.ContentGenerationApi;
import com.datn.datnbe.ai.api.TokenUsageApi;
import com.datn.datnbe.ai.dto.request.AIGatewayGenerateByTopicRequest;
import com.datn.datnbe.ai.dto.request.AIGatewayGenerateQuestionsFromContextRequest;
import com.datn.datnbe.ai.dto.request.GenerateQuestionsFromMatrixRequest;
import com.datn.datnbe.ai.dto.response.TokenUsageInfoDto;
import com.datn.datnbe.ai.entity.TokenUsage;
import com.datn.datnbe.ai.service.PhoenixQueryService;
import com.datn.datnbe.document.dto.request.GenerateQuestionsByTopicRequest;
import com.datn.datnbe.document.dto.request.GenerateQuestionsFromContextRequest;
import com.datn.datnbe.document.dto.response.GenerateQuestionsByTopicResponse;
import com.datn.datnbe.document.dto.response.QuestionResponseDto;
import com.datn.datnbe.document.entity.Context;
import com.datn.datnbe.document.entity.Question;
import com.datn.datnbe.document.entity.QuestionBankItem;
import com.datn.datnbe.document.dto.request.GenerateQuestionsFromTopicRequest;
import com.datn.datnbe.document.dto.response.GeneratedQuestionsResponse;
import com.datn.datnbe.document.repository.ContextRepository;
import com.datn.datnbe.document.management.ChapterManagement;
import com.datn.datnbe.document.entity.questiondata.QuestionType;
import com.datn.datnbe.document.mapper.QuestionEntityMapper;
import com.datn.datnbe.document.repository.QuestionRepository;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class QuestionGenerationService {

    ContentGenerationApi contentGenerationApi;
    QuestionRepository questionRepository;
    QuestionEntityMapper questionMapper;
    ObjectMapper objectMapper;
    PhoenixQueryService phoenixQueryService;
    TokenUsageApi tokenUsageApi;
    CoinPricingApi coinPricingApi;
    ContextRepository contextRepository;
    ChapterManagement chapterManagement;

    @Transactional
    public GeneratedQuestionsResponse generateAndSaveQuestions(GenerateQuestionsFromTopicRequest request,
            String teacherId) {

        log.info("Generating questions for teacher: {}", teacherId);

        // Call AI service to generate questions (returns JSON string)
        String traceId = java.util.UUID.randomUUID().toString();
        String jsonResult = contentGenerationApi.generateQuestions(request, traceId);
        extractAndSaveTokenUsage(traceId, request, "question", teacherId);
        log.info("AI response received, length: {} chars", jsonResult != null ? jsonResult.length() : 0);

        // Parse JSON string to list of Question POJOs
        List<Question> aiQuestions;
        try {
            // Configure ObjectMapper to be lenient with unknown properties
            ObjectMapper lenientMapper = objectMapper.copy();
            lenientMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                    false);
            lenientMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY,
                    true);

            aiQuestions = lenientMapper.readValue(jsonResult, new TypeReference<List<Question>>() {
            });
            log.info("Parsed {} questions from AI response", aiQuestions.size());

            // Log first question details for debugging
            if (!aiQuestions.isEmpty()) {
                Question firstQ = aiQuestions.get(0);
                log.debug("First question sample - type: {}, difficulty: {}, title: {}, data type: {}, data value: {}",
                        firstQ.getType(),
                        firstQ.getDifficulty(),
                        firstQ.getTitle() != null
                                ? firstQ.getTitle().substring(0, Math.min(50, firstQ.getTitle().length()))
                                : "null",
                        firstQ.getData() != null ? firstQ.getData().getClass().getSimpleName() : "null",
                        firstQ.getData());

                // For FILL_IN_BLANK, log the data structure
                if (QuestionType.FILL_IN_BLANK.equals(firstQ.getType())) {
                    log.debug("FILL_IN_BLANK data details: {}", firstQ.getData());
                    if (firstQ.getData() instanceof java.util.Map) {
                        java.util.Map<?, ?> dataMap = (java.util.Map<?, ?>) firstQ.getData();
                        log.debug("FILL_IN_BLANK data keys: {}", dataMap.keySet());
                        log.debug("FILL_IN_BLANK 'data' field value: {}", dataMap.get("data"));
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse AI response. Error: {}, Response preview: {}",
                    e.getMessage(),
                    jsonResult != null && jsonResult.length() > 500
                            ? jsonResult.substring(0, 500) + "..."
                            : jsonResult);
            log.debug("Full AI response that failed to parse: {}", jsonResult);
            throw new AppException(ErrorCode.AI_WORKER_SERVER_ERROR, "Failed to parse AI response: " + e.getMessage());
        }

        // Validate questions with detailed error messages
        List<String> validationErrors = new java.util.ArrayList<>();
        for (int i = 0; i < aiQuestions.size(); i++) {
            Question q = aiQuestions.get(i);
            int questionNum = i + 1;

            // Validate required fields
            if (q.getType() == null)
                validationErrors.add("Question " + questionNum + ": missing type");
            if (q.getDifficulty() == null)
                validationErrors.add("Question " + questionNum + ": missing difficulty");
            if (q.getTitle() == null || q.getTitle().trim().isEmpty())
                validationErrors.add("Question " + questionNum + ": missing or empty title");
            if (q.getSubject() == null || q.getSubject().trim().isEmpty())
                validationErrors.add("Question " + questionNum + ": missing or empty subject");
            if (q.getGrade() == null || q.getGrade().trim().isEmpty())
                validationErrors.add("Question " + questionNum + ": missing or empty grade");
            if (q.getData() == null)
                validationErrors.add("Question " + questionNum + ": missing data");

            if (validationErrors.isEmpty()) {
                log.info("Valid question {}: type={}, difficulty={}, title={}, subject={}, grade={}",
                        questionNum,
                        q.getType(),
                        q.getDifficulty(),
                        q.getTitle(),
                        q.getSubject(),
                        q.getGrade());
            }
        }

        if (!validationErrors.isEmpty()) {
            String errorMessage = "Question validation failed:\n" + String.join("\n", validationErrors);
            log.error(errorMessage);
            throw new AppException(ErrorCode.INVALID_ELEMENT_DATA, errorMessage);
        }

        // Convert to QuestionBankItem entities (without IDs - let JPA generate them)
        List<QuestionBankItem> questionEntities = aiQuestions.stream()
                .map(q -> convertToQuestionBankItem(q, teacherId))
                .collect(Collectors.toList());

        log.info("Saving {} questions to database", questionEntities.size());

        // Batch save all questions
        List<QuestionBankItem> savedQuestions;
        try {
            savedQuestions = questionRepository.saveAll(questionEntities);
            log.info("Successfully saved {} questions", savedQuestions.size());
        } catch (Exception e) {
            log.error("Failed to save questions to database. Error type: {}, Message: {}",
                    e.getClass().getSimpleName(),
                    e.getMessage());
            throw new AppException(ErrorCode.AI_WORKER_SERVER_ERROR,
                    "Failed to save questions to database: " + e.getMessage());
        }

        // Extract IDs and full question details
        List<QuestionResponseDto> questionDetails = savedQuestions.stream()
                .map(questionMapper::toResponseDto)
                .collect(Collectors.toList());

        return GeneratedQuestionsResponse.builder()
                .totalGenerated(savedQuestions.size())
                .questions(questionDetails)
                .build();
    }

    @Transactional
    public GeneratedQuestionsResponse generateAndSaveQuestionsFromContext(GenerateQuestionsFromContextRequest request,
            String teacherId) {

        log.info("Generating questions from context for teacher: {}, contextId: {}", teacherId, request.getContextId());

        // 1. Resolve context: fetch from DB or use inline content
        String contextContent;
        String contextGrade;
        String contextSubject;
        String contextId = request.getContextId();

        if (contextId != null && !contextId.isBlank()) {
            Context context = contextRepository.findById(contextId)
                    .orElseThrow(() -> new AppException(ErrorCode.CONTEXT_NOT_FOUND));
            contextContent = context.getContent();
            contextGrade = context.getGrade();
            contextSubject = context.getSubject();
        } else if (request.getContextContent() != null && !request.getContextContent().isBlank()) {
            contextContent = request.getContextContent();
            contextGrade = request.getGrade();
            contextSubject = request.getSubject();
            contextId = null;
        } else {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Either contextId or contextContent must be provided");
        }

        // Parse "count:points" cell strings into structured QuestionRequirement
        // objects,
        // consistent with GenerateQuestionsFromMatrixRequest.TopicRequirement.
        Map<String, Map<String, GenerateQuestionsFromMatrixRequest.QuestionRequirement>> parsedQuestionsPerDifficulty = request
                .getQuestionsPerDifficulty()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        diffEntry -> diffEntry.getValue()
                                .entrySet()
                                .stream()
                                .collect(Collectors.toMap(Map.Entry::getKey, typeEntry -> {
                                    String[] parts = typeEntry.getValue().split(":");
                                    return GenerateQuestionsFromMatrixRequest.QuestionRequirement.builder()
                                            .count(Integer.parseInt(parts[0]))
                                            .points(Double.parseDouble(parts[1]))
                                            .build();
                                }))));

        AIGatewayGenerateQuestionsFromContextRequest aiRequest = AIGatewayGenerateQuestionsFromContextRequest.builder()
                .context(contextContent)
                .contextType("TEXT")
                .grade(contextGrade)
                .subject(contextSubject)
                .questionsPerDifficulty(parsedQuestionsPerDifficulty)
                .prompt(request.getPrompt())
                .provider(request.getProvider() != null ? request.getProvider().toLowerCase() : "google")
                .model(request.getModel() != null ? request.getModel().toLowerCase() : "gemini-2.5-flash")
                .build();

        // 4. Call AI service
        String traceId = java.util.UUID.randomUUID().toString();
        String jsonResult = contentGenerationApi.generateQuestionsFromContext(aiRequest, traceId);
        log.info("AI response received, length: {} chars", jsonResult != null ? jsonResult.length() : 0);

        // 5. Parse JSON response
        List<Question> aiQuestions;
        try {
            ObjectMapper lenientMapper = objectMapper.copy();
            lenientMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                    false);
            lenientMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY,
                    true);
            aiQuestions = lenientMapper.readValue(jsonResult, new TypeReference<List<Question>>() {
            });
            log.info("Parsed {} questions from AI response", aiQuestions.size());
        } catch (Exception e) {
            log.error("Failed to parse AI response: {}", e.getMessage());
            throw new AppException(ErrorCode.AI_WORKER_SERVER_ERROR, "Failed to parse AI response: " + e.getMessage());
        }

        // 6. Convert and enrich with contextId (if available)
        final String resolvedContextId = contextId;
        List<QuestionBankItem> questionEntities = aiQuestions.stream().map(q -> {
            QuestionBankItem item = convertToQuestionBankItem(q, teacherId);
            if (resolvedContextId != null) {
                item.setContextId(resolvedContextId);
            }
            return item;
        }).collect(Collectors.toList());

        log.info("Saving {} context-based questions to database", questionEntities.size());

        List<QuestionBankItem> savedQuestions;
        try {
            savedQuestions = questionRepository.saveAll(questionEntities);
            log.info("Successfully saved {} questions", savedQuestions.size());
        } catch (Exception e) {
            log.error("Failed to save questions to database: {}", e.getMessage());
            throw new AppException(ErrorCode.AI_WORKER_SERVER_ERROR,
                    "Failed to save questions to database: " + e.getMessage());
        }

        List<QuestionResponseDto> questionDetails = savedQuestions.stream()
                .map(questionMapper::toResponseDto)
                .collect(Collectors.toList());

        return GeneratedQuestionsResponse.builder()
                .totalGenerated(savedQuestions.size())
                .questions(questionDetails)
                .build();
    }

    private QuestionBankItem convertToQuestionBankItem(Question aiQuestion, String ownerId) {
        // Use topic name directly as chapter field (String)
        // The chapter field stores the topic name for filtering by grade, subject, and
        // topic
        String topicName = aiQuestion.getChapter();

        String chapterId = null;
        if (topicName != null && !topicName.isEmpty()) {
            log.debug("Setting topic name (chapter): '{}', subject: '{}', grade: '{}'",
                    topicName,
                    aiQuestion.getSubject(),
                    aiQuestion.getGrade());
            chapterId = chapterManagement.getChapterId(topicName);
        }

        // Convert and parse the data based on question type
        Object convertedData = questionMapper.convertDataToQuestionData(aiQuestion.getData(),
                aiQuestion.getType().name());

        return QuestionBankItem.builder()
                // DO NOT set id - let JPA generate it
                .type(aiQuestion.getType())
                .difficulty(aiQuestion.getDifficulty())
                .title(aiQuestion.getTitle())
                .titleImageUrl(aiQuestion.getTitleImageUrl())
                .explanation(aiQuestion.getExplanation())
                .grade(aiQuestion.getGrade())
                .chapter(topicName) // Store topic name directly as String
                .chapterId(chapterId)
                .subject(aiQuestion.getSubject())
                .data(convertedData)
                .ownerId(ownerId)
                // createdAt and updatedAt are auto-generated
                .build();
    }

    /**
     * Generate questions for a single topic from the assignment matrix and return directly
     * (questions are NOT persisted to the question bank).
     * <p>
     * If {@code hasContext=true}, a context (reading passage) is randomly selected from the DB
     * and up to 7 questions use it. Remaining questions are generated normally.
     * Context-based questions have their {@code contextId} set in the response.
     */
    public GenerateQuestionsByTopicResponse generateQuestionsByTopic(GenerateQuestionsByTopicRequest request,
            String userId) {

        log.info("[BY_TOPIC] Generating questions for topic: {}, grade: {}, hasContext: {}",
                request.getTopicName(),
                request.getGrade(),
                request.isHasContext());

        // 1. Parse and order all "count:points" cells
        //    Order: KNOWLEDGE → COMPREHENSION → APPLICATION, within each difficulty by insertion order
        List<String[]> orderedCells = new ArrayList<>(); // [difficulty, questionType, count, points]
        int totalQuestions = 0;

        String[] difficultyOrder = {"KNOWLEDGE", "COMPREHENSION", "APPLICATION"};
        for (String difficulty : difficultyOrder) {
            Map<String, String> typeMap = request.getQuestionsPerDifficulty().get(difficulty);
            if (typeMap == null)
                continue;
            for (Map.Entry<String, String> entry : typeMap.entrySet()) {
                String[] parts = entry.getValue().split(":");
                int count = Integer.parseInt(parts[0]);
                if (count > 0) {
                    orderedCells.add(new String[]{difficulty, entry.getKey(), parts[0], parts[1]});
                    totalQuestions += count;
                }
            }
        }

        if (totalQuestions == 0) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Total questions must be greater than 0");
        }

        // 2. Build groups for the gateway request
        final int MAX_CONTEXT_QUESTIONS = 7;
        Context selectedContext = null;
        List<AIGatewayGenerateByTopicRequest.Group> groups = new ArrayList<>();

        if (request.isHasContext()) {
            List<Context> contexts = contextRepository.findByGradeAndSubject(request.getGrade(), request.getSubject());
            if (contexts.isEmpty()) {
                throw new AppException(ErrorCode.CONTEXT_NOT_FOUND, "No reading passages found for grade "
                        + request.getGrade() + ", subject " + request.getSubject());
            }
            selectedContext = contexts.get(new Random().nextInt(contexts.size()));

            // Split cells: first MAX_CONTEXT_QUESTIONS questions → CONTEXT group, rest → NORMAL group
            Map<String, Map<String, GenerateQuestionsFromMatrixRequest.QuestionRequirement>> contextReqs = new LinkedHashMap<>();
            Map<String, Map<String, GenerateQuestionsFromMatrixRequest.QuestionRequirement>> normalReqs = new LinkedHashMap<>();
            int accumulated = 0;

            for (String[] cell : orderedCells) {
                String diff = cell[0], qType = cell[1];
                int count = Integer.parseInt(cell[2]);
                double points = Double.parseDouble(cell[3]);

                if (accumulated >= MAX_CONTEXT_QUESTIONS) {
                    // All go to normal
                    normalReqs.computeIfAbsent(diff, k -> new LinkedHashMap<>())
                            .put(qType,
                                    GenerateQuestionsFromMatrixRequest.QuestionRequirement.builder()
                                            .count(count)
                                            .points(points)
                                            .build());
                } else if (accumulated + count <= MAX_CONTEXT_QUESTIONS) {
                    // Entire cell goes to context
                    contextReqs.computeIfAbsent(diff, k -> new LinkedHashMap<>())
                            .put(qType,
                                    GenerateQuestionsFromMatrixRequest.QuestionRequirement.builder()
                                            .count(count)
                                            .points(points)
                                            .build());
                    accumulated += count;
                } else {
                    // Split: part to context, rest to normal
                    int contextCount = MAX_CONTEXT_QUESTIONS - accumulated;
                    int normalCount = count - contextCount;
                    contextReqs.computeIfAbsent(diff, k -> new LinkedHashMap<>())
                            .put(qType,
                                    GenerateQuestionsFromMatrixRequest.QuestionRequirement.builder()
                                            .count(contextCount)
                                            .points(points)
                                            .build());
                    normalReqs.computeIfAbsent(diff, k -> new LinkedHashMap<>())
                            .put(qType,
                                    GenerateQuestionsFromMatrixRequest.QuestionRequirement.builder()
                                            .count(normalCount)
                                            .points(points)
                                            .build());
                    accumulated = MAX_CONTEXT_QUESTIONS;
                }
            }

            // CONTEXT group (group index 0)
            groups.add(AIGatewayGenerateByTopicRequest.Group.builder()
                    .groupType("CONTEXT")
                    .contextContent(selectedContext.getContent())
                    .contextType("TEXT")
                    .requirements(contextReqs)
                    .build());

            // NORMAL group if there are remaining questions (group index 1)
            if (!normalReqs.isEmpty()) {
                groups.add(AIGatewayGenerateByTopicRequest.Group.builder()
                        .groupType("NORMAL")
                        .requirements(normalReqs)
                        .build());
            }
        } else {
            // No context — single NORMAL group with all questions (group index 0)
            Map<String, Map<String, GenerateQuestionsFromMatrixRequest.QuestionRequirement>> allReqs = new LinkedHashMap<>();
            for (String[] cell : orderedCells) {
                allReqs.computeIfAbsent(cell[0], k -> new LinkedHashMap<>())
                        .put(cell[1],
                                GenerateQuestionsFromMatrixRequest.QuestionRequirement.builder()
                                        .count(Integer.parseInt(cell[2]))
                                        .points(Double.parseDouble(cell[3]))
                                        .build());
            }
            groups.add(
                    AIGatewayGenerateByTopicRequest.Group.builder().groupType("NORMAL").requirements(allReqs).build());
        }

        // 3. Call GenAI Gateway
        AIGatewayGenerateByTopicRequest gatewayRequest = AIGatewayGenerateByTopicRequest.builder()
                .grade(request.getGrade())
                .subject(request.getSubject())
                .topicName(request.getTopicName())
                .groups(groups)
                .provider(request.getProvider() != null ? request.getProvider().toLowerCase() : "google")
                .model(request.getModel() != null ? request.getModel().toLowerCase() : "gemini-2.5-flash")
                .build();

        String traceId = java.util.UUID.randomUUID().toString();
        String jsonResult = contentGenerationApi.generateQuestionsByTopic(gatewayRequest, traceId);
        log.info("[BY_TOPIC] AI response received, length: {} chars", jsonResult != null ? jsonResult.length() : 0);

        // 4. Parse response
        List<Question> aiQuestions;
        try {
            ObjectMapper lenientMapper = objectMapper.copy();
            lenientMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                    false);
            lenientMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY,
                    true);
            aiQuestions = lenientMapper.readValue(jsonResult, new TypeReference<List<Question>>() {
            });
            log.info("[BY_TOPIC] Parsed {} questions from AI response", aiQuestions.size());
        } catch (Exception e) {
            log.error("[BY_TOPIC] Failed to parse AI response. Error: {}, Preview: {}",
                    e.getMessage(),
                    jsonResult != null && jsonResult.length() > 500
                            ? jsonResult.substring(0, 500) + "..."
                            : jsonResult);
            throw new AppException(ErrorCode.AI_WORKER_SERVER_ERROR, "Failed to parse AI response: " + e.getMessage());
        }

        // 5. Enrich questions and build response DTOs (no DB save)
        final String contextId = selectedContext != null ? selectedContext.getId() : null;
        final boolean hasContextGroup = request.isHasContext();

        List<QuestionResponseDto> questionDtos = aiQuestions.stream().map(q -> {
            // Fill in fields the gateway omitted
            q.setGrade(request.getGrade());
            q.setSubject(request.getSubject());
            q.setChapter(request.getTopicName());

            // Assign contextId to questions in group 0 when hasContext=true
            if (hasContextGroup && contextId != null && (q.getGroup() == null || q.getGroup() == 0)) {
                q.setContextId(contextId);
            }

            // Convert data field to domain format
            Object convertedData = questionMapper.convertDataToQuestionData(q.getData(),
                    q.getType() != null ? q.getType().name() : null);

            return QuestionResponseDto.builder()
                    .title(q.getTitle())
                    .type(q.getType() != null ? q.getType().name() : null)
                    .difficulty(q.getDifficulty() != null ? q.getDifficulty().name() : null)
                    .explanation(q.getExplanation())
                    .titleImageUrl(q.getTitleImageUrl())
                    .grade(q.getGrade())
                    .chapter(q.getChapter())
                    .subject(q.getSubject())
                    .contextId(q.getContextId())
                    .data(convertedData)
                    .build();
        }).collect(Collectors.toList());

        // 6. Build selected context info for response
        GenerateQuestionsByTopicResponse.SelectedContextDto contextDto = null;
        if (selectedContext != null) {
            contextDto = GenerateQuestionsByTopicResponse.SelectedContextDto.builder()
                    .id(selectedContext.getId())
                    .title(selectedContext.getTitle())
                    .build();
        }

        log.info("[BY_TOPIC] Successfully built {} question DTOs", questionDtos.size());
        return GenerateQuestionsByTopicResponse.builder()
                .totalGenerated(questionDtos.size())
                .questions(questionDtos)
                .context(contextDto)
                .build();
    }

    @Async
    protected void extractAndSaveTokenUsage(String traceId,
            GenerateQuestionsFromTopicRequest request,
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
