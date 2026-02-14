package com.datn.datnbe.document.service;

import com.datn.datnbe.ai.api.CoinPricingApi;
import com.datn.datnbe.ai.api.ContentGenerationApi;
import com.datn.datnbe.ai.api.TokenUsageApi;
import com.datn.datnbe.ai.dto.response.TokenUsageInfoDto;
import com.datn.datnbe.ai.entity.TokenUsage;
import com.datn.datnbe.ai.service.PhoenixQueryService;
import com.datn.datnbe.document.dto.response.QuestionResponseDto;
import com.datn.datnbe.document.entity.Question;
import com.datn.datnbe.document.entity.QuestionBankItem;
import com.datn.datnbe.document.dto.request.GenerateQuestionsFromTopicRequest;
import com.datn.datnbe.document.dto.response.GeneratedQuestionsResponse;
import com.datn.datnbe.document.entity.questiondata.FillInBlankData;
import com.datn.datnbe.document.entity.questiondata.MatchingData;
import com.datn.datnbe.document.entity.questiondata.MatchingPair;
import com.datn.datnbe.document.entity.questiondata.MultipleChoiceData;
import com.datn.datnbe.document.entity.questiondata.MultipleChoiceOption;
import com.datn.datnbe.document.entity.questiondata.OpenEndedData;
import com.datn.datnbe.document.entity.questiondata.QuestionType;
import com.datn.datnbe.document.mapper.QuestionEntityMapper;
import com.datn.datnbe.document.repository.QuestionRepository;
import com.datn.datnbe.document.utils.FillInBlankParser;
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

import java.util.List;
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

    private QuestionBankItem convertToQuestionBankItem(Question aiQuestion, String ownerId) {
        // Use topic name directly as chapter field (String)
        // The chapter field stores the topic name for filtering by grade, subject, and topic
        String topicName = aiQuestion.getChapter();

        if (topicName != null && !topicName.isEmpty()) {
            log.debug("Setting topic name (chapter): '{}', subject: '{}', grade: '{}'",
                    topicName,
                    aiQuestion.getSubject(),
                    aiQuestion.getGrade());
        }

        // Convert and parse the data based on question type
        Object convertedData = convertQuestionData(aiQuestion.getData(), aiQuestion.getType());

        return QuestionBankItem.builder()
                // DO NOT set id - let JPA generate it
                .type(aiQuestion.getType())
                .difficulty(aiQuestion.getDifficulty())
                .title(aiQuestion.getTitle())
                .titleImageUrl(aiQuestion.getTitleImageUrl())
                .explanation(aiQuestion.getExplanation())
                .grade(aiQuestion.getGrade())
                .chapter(topicName) // Store topic name directly as String
                .subject(aiQuestion.getSubject())
                .data(convertedData)
                .ownerId(ownerId)
                // createdAt and updatedAt are auto-generated
                .build();
    }

    private Object convertQuestionData(Object data, QuestionType type) {
        if (type == null) {
            log.warn("Question type is null, cannot convert data");
            return data;
        }

        switch (type) {
            case FILL_IN_BLANK :
                return convertFillInBlankData(data);
            case MULTIPLE_CHOICE :
                return convertMultipleChoiceData(data);
            case MATCHING :
                return convertMatchingData(data);
            case OPEN_ENDED :
                return convertOpenEndedData(data);
            default :
                log.warn("Unhandled question type: {}", type);
                return data;
        }
    }

    private Object convertFillInBlankData(Object data) {
        try {
            log.debug("Converting FILL_IN_BLANK data. Type: {}", data != null ? data.getClass().getName() : "null");

            String textContent = null;
            Boolean caseSensitive = false;

            // Handle FillInBlankData from AI Gateway
            if (data instanceof com.datn.datnbe.ai.dto.response.FillInBlankData) {
                com.datn.datnbe.ai.dto.response.FillInBlankData aiData = (com.datn.datnbe.ai.dto.response.FillInBlankData) data;
                textContent = aiData.getData();
                caseSensitive = aiData.getCaseSensitive() != null ? aiData.getCaseSensitive() : false;
                log.debug("Processing AI FillInBlankData. Text: {}, CaseSensitive: {}", textContent, caseSensitive);
            } else if (data instanceof java.util.Map) {
                // Fallback for Map format
                @SuppressWarnings("unchecked") java.util.Map<String, Object> dataMap = (java.util.Map<String, Object>) data;
                Object nestedData = dataMap.get("data");
                if (nestedData instanceof String) {
                    textContent = (String) nestedData;
                }
                Object caseSensitiveObj = dataMap.get("caseSensitive");
                if (caseSensitiveObj instanceof Boolean) {
                    caseSensitive = (Boolean) caseSensitiveObj;
                }
                log.debug("Processing Map format. Text: {}, CaseSensitive: {}", textContent, caseSensitive);
            } else if (data instanceof String) {
                // Legacy string format
                textContent = (String) data;
                log.debug("Processing legacy string format: {}", textContent);
            } else {
                log.warn("Unexpected FILL_IN_BLANK data type: {}", data != null ? data.getClass().getName() : "null");
            }

            if (textContent != null && !textContent.isBlank()) {
                log.debug("Parsing FILL_IN_BLANK text: {}", textContent);
                FillInBlankData fillInBlankData = FillInBlankParser.parse(textContent);
                fillInBlankData.setCaseSensitive(caseSensitive);
                log.info("Successfully parsed FILL_IN_BLANK, segments: {}", fillInBlankData.getSegments().size());
                return fillInBlankData;
            }

            log.error("No valid text content in FILL_IN_BLANK data");
            return data;
        } catch (Exception e) {
            log.error("Failed to parse FILL_IN_BLANK data: {}", e.getMessage(), e);
            return data;
        }
    }

    private Object convertMultipleChoiceData(Object data) {
        try {
            log.debug("Converting MULTIPLE_CHOICE data. Type: {}", data != null ? data.getClass().getName() : "null");

            if (data instanceof com.datn.datnbe.ai.dto.response.MultipleChoiceData) {
                com.datn.datnbe.ai.dto.response.MultipleChoiceData aiData = (com.datn.datnbe.ai.dto.response.MultipleChoiceData) data;
                List<MultipleChoiceOption> options = aiData.getOptions()
                        .stream()
                        .map(aiOption -> MultipleChoiceOption.builder()
                                .id(java.util.UUID.randomUUID().toString())
                                .text(aiOption.getText())
                                .imageUrl(aiOption.getImageUrl())
                                .isCorrect(aiOption.getIsCorrect())
                                .build())
                        .collect(java.util.stream.Collectors.toList());

                return MultipleChoiceData.builder()
                        .options(options)
                        .shuffleOptions(aiData.getShuffleOptions() != null ? aiData.getShuffleOptions() : false)
                        .build();
            }

            log.warn("MULTIPLE_CHOICE data is not AI MultipleChoiceData: {}",
                    data != null ? data.getClass().getName() : "null");
            return data;
        } catch (Exception e) {
            log.error("Failed to convert MULTIPLE_CHOICE data: {}", e.getMessage(), e);
            return data;
        }
    }

    private Object convertMatchingData(Object data) {
        try {
            log.debug("Converting MATCHING data. Type: {}", data != null ? data.getClass().getName() : "null");

            if (data instanceof com.datn.datnbe.ai.dto.response.MatchingData) {
                com.datn.datnbe.ai.dto.response.MatchingData aiData = (com.datn.datnbe.ai.dto.response.MatchingData) data;
                List<MatchingPair> pairs = aiData.getPairs()
                        .stream()
                        .map(aiPair -> MatchingPair.builder()
                                .id(java.util.UUID.randomUUID().toString())
                                .left(aiPair.getLeft())
                                .leftImageUrl(aiPair.getLeftImageUrl())
                                .right(aiPair.getRight())
                                .rightImageUrl(aiPair.getRightImageUrl())
                                .build())
                        .collect(java.util.stream.Collectors.toList());

                return MatchingData.builder()
                        .pairs(pairs)
                        .shufflePairs(aiData.getShufflePairs() != null ? aiData.getShufflePairs() : false)
                        .build();
            }

            log.warn("MATCHING data is not AI MatchingData: {}", data != null ? data.getClass().getName() : "null");
            return data;
        } catch (Exception e) {
            log.error("Failed to convert MATCHING data: {}", e.getMessage(), e);
            return data;
        }
    }

    private Object convertOpenEndedData(Object data) {
        try {
            log.debug("Converting OPEN_ENDED data. Type: {}", data != null ? data.getClass().getName() : "null");

            if (data instanceof com.datn.datnbe.ai.dto.response.OpenEndedData) {
                com.datn.datnbe.ai.dto.response.OpenEndedData aiData = (com.datn.datnbe.ai.dto.response.OpenEndedData) data;
                return OpenEndedData.builder()
                        .expectedAnswer(aiData.getExpectedAnswer())
                        .maxLength(aiData.getMaxLength())
                        .build();
            }

            log.warn("OPEN_ENDED data is not AI OpenEndedData: {}", data != null ? data.getClass().getName() : "null");
            return data;
        } catch (Exception e) {
            log.error("Failed to convert OPEN_ENDED data: {}", e.getMessage(), e);
            return data;
        }
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
