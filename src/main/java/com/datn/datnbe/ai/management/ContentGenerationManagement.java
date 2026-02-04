package com.datn.datnbe.ai.management;

import com.datn.datnbe.ai.api.ContentGenerationApi;
import com.datn.datnbe.ai.api.ModelSelectionApi;
import com.datn.datnbe.ai.api.TokenUsageApi;
import com.datn.datnbe.ai.apiclient.AIApiClient;
import com.datn.datnbe.ai.dto.request.AIWorkerGenerateQuestionsRequest;
import com.datn.datnbe.ai.dto.request.MindmapPromptRequest;
import com.datn.datnbe.ai.dto.request.OutlinePromptRequest;
import com.datn.datnbe.ai.dto.request.PresentationPromptRequest;
import com.datn.datnbe.ai.dto.response.AiWokerResponse;
import com.datn.datnbe.ai.entity.TokenUsage;
import com.datn.datnbe.ai.utils.MappingParamsUtils;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import com.datn.datnbe.sharedkernel.security.utils.SecurityContextUtils;
import com.datn.datnbe.document.exam.dto.ExamMatrixDto;
import com.datn.datnbe.document.exam.dto.request.GenerateMatrixRequest;
import com.datn.datnbe.document.exam.dto.request.GenerateQuestionsFromTopicRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ContentGenerationManagement implements ContentGenerationApi {
    ModelSelectionApi modelSelectionApi;
    AIApiClient aiApiClient;
    TokenUsageApi tokenUsageApi;
    SecurityContextUtils securityContextUtils;

    @Value("${ai.api.outline-endpoint}")
    @NonFinal
    String OUTLINE_API_ENDPOINT;

    @Value("${ai.api.outline-batch-endpoint}")
    @NonFinal
    String OUTLINE_BATCH_API_ENDPOINT;

    @Value("${ai.api.presentation-endpoint}")
    @NonFinal
    String PRESENTATION_API_ENDPOINT;

    @Value("${ai.api.presentation-batch-endpoint}")
    @NonFinal
    String PRESENTATION_BATCH_API_ENDPOINT;

    @Value("${ai.api.mindmap-endpoint}")
    @NonFinal
    String MINDMAP_API_ENDPOINT;

    @Value("${ai.api.exam-matrix-endpoint}")
    @NonFinal
    String EXAM_MATRIX_API_ENDPOINT;

    @Value("${ai.api.questions-endpoint:/api/questions/generate}")
    @NonFinal
    String QUESTIONS_API_ENDPOINT;

    @Override
    public Flux<String> generateOutline(OutlinePromptRequest request) {

        if (!modelSelectionApi.isModelEnabled(request.getModel())) {
            log.error("Model {} is not enabled for outline generation", request.getModel());
            return Flux.error(new AppException(ErrorCode.MODEL_NOT_ENABLED));
        }

        log.info("Calling AI to stream outline generation");
        return aiApiClient.postSse(OUTLINE_API_ENDPOINT, MappingParamsUtils.constructParams(request))
                .map(chunk -> new String(Base64.getDecoder().decode(chunk), StandardCharsets.UTF_8));
    }

    @Override
    public Flux<String> generateSlides(PresentationPromptRequest request) {
        log.info("Starting streaming presentation generation for slides");

        if (!modelSelectionApi.isModelEnabled(request.getModel())) {
            log.error("Model {} is not enabled for slide generation", request.getModel());
            return Flux.error(new AppException(ErrorCode.MODEL_NOT_ENABLED));
        }

        log.info("Calling AI to stream presentation slides");

        return aiApiClient.postSse(PRESENTATION_API_ENDPOINT, MappingParamsUtils.constructParams(request));
    }

    @Override
    public String generateOutlineBatch(OutlinePromptRequest request) {
        log.info("Starting batch outline generation");

        if (!modelSelectionApi.isModelEnabled(request.getModel())) {
            throw new AppException(ErrorCode.MODEL_NOT_ENABLED);
        }

        log.info("Calling AI to generate outline in batch mode");
        try {
            AiWokerResponse response = aiApiClient.post(OUTLINE_BATCH_API_ENDPOINT,
                    MappingParamsUtils.constructParams(request),
                    AiWokerResponse.class);

            saveTokenUsageIfPresent(response, "OUTLINE");

            log.info("Batch outline generation completed successfully");
            return response.getData();
        } catch (Exception e) {
            log.error("Error during batch outline generation", e);
            throw new AppException(ErrorCode.AI_WORKER_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    public String generateSlidesBatch(PresentationPromptRequest request) {
        log.info("Starting batch presentation generation for slides");

        if (!modelSelectionApi.isModelEnabled(request.getModel())) {
            log.error("Model {} is not enabled for slide generation", request.getModel());
            throw new AppException(ErrorCode.MODEL_NOT_ENABLED);
        }

        log.info("Calling AI to generate presentation slides in batch mode");
        try {
            AiWokerResponse response = aiApiClient.post(PRESENTATION_BATCH_API_ENDPOINT,
                    MappingParamsUtils.constructParams(request),
                    AiWokerResponse.class);

            saveTokenUsageIfPresent(response, "PRESENTATION");

            log.info("Batch presentation generation completed successfully");
            return response.getData();
        } catch (Exception e) {
            log.error("Error during batch presentation generation", e);
            throw new AppException(ErrorCode.AI_WORKER_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    public String generateMindmap(MindmapPromptRequest request) {
        log.info("Starting mindmap generation");

        if (!modelSelectionApi.isModelEnabled(request.getModel())) {
            throw new AppException(ErrorCode.MODEL_NOT_ENABLED);
        }

        log.info("Calling AI to generate mindmap");
        try {
            AiWokerResponse response = aiApiClient
                    .post(MINDMAP_API_ENDPOINT, MappingParamsUtils.constructParams(request), AiWokerResponse.class);

            saveTokenUsageIfPresent(response, "MINDMAP");

            log.info("Mindmap generation completed successfully");
            return response.getData();
        } catch (Exception e) {
            log.error("Error during mindmap generation", e);
            throw new AppException(ErrorCode.AI_WORKER_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    public ExamMatrixDto generateExamMatrix(GenerateMatrixRequest request) {
        // TODO: select list topics from db
        log.info("Starting exam matrix generation for topics: {}", request.getTopics());

        if (!modelSelectionApi.isModelEnabled(request.getModel())) {
            throw new AppException(ErrorCode.MODEL_NOT_ENABLED);
        }

        log.info("Calling AI to generate exam matrix via: {}", EXAM_MATRIX_API_ENDPOINT);
        try {
            ExamMatrixDto result = aiApiClient.post(EXAM_MATRIX_API_ENDPOINT, request, ExamMatrixDto.class);
            log.info("Exam matrix generation completed successfully");
            return result;
        } catch (Exception e) {
            log.error("Error during exam matrix generation", e);
            throw new AppException(ErrorCode.AI_WORKER_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    public String generateQuestions(GenerateQuestionsFromTopicRequest request) {
        log.info("Generating questions for topic: {}, grade: {}", request.getTopic(), request.getGrade());

        // Transform request to GenAI-Gateway format
        List<String> questionTypesList = request.getQuestionTypes()
                .stream()
                .map(qt -> qt.name()) // Keep uppercase for GenAI-Gateway
                .collect(Collectors.toList());

        // Convert difficulty keys to uppercase for GenAI-Gateway
        Map<String, Integer> difficultyMap = new HashMap<>();
        request.getQuestionsPerDifficulty()
                .forEach((difficulty, count) -> difficultyMap.put(difficulty.toUpperCase(), count));

        AIWorkerGenerateQuestionsRequest aiRequest = AIWorkerGenerateQuestionsRequest.builder()
                .topic(request.getTopic())
                .grade(request.getGrade())
                .subject(request.getSubject())
                .questionsPerDifficulty(difficultyMap)
                .questionTypes(questionTypesList)
                .additionalRequirements(request.getAdditionalRequirements())
                .provider(request.getProvider() != null ? request.getProvider() : "google")
                .model(request.getModel() != null ? request.getModel() : "gemini-2.5-flash-lite")
                .build();

        // Make synchronous call to GenAI-Gateway - return raw JSON string
        log.info("Calling GenAI-Gateway at endpoint: {}", QUESTIONS_API_ENDPOINT);
        try {
            String jsonResponse = aiApiClient.post(QUESTIONS_API_ENDPOINT, aiRequest, String.class);

            log.info("Successfully received GenAI-Gateway response");
            return jsonResponse;
        } catch (Exception e) {
            log.error("Error during question generation", e);
            throw new AppException(ErrorCode.AI_WORKER_SERVER_ERROR, e.getMessage());
        }
    }

    private void saveTokenUsageIfPresent(AiWokerResponse response, String requestType) {
        try {
            if (response != null && response.getTokenUsage() != null) {
                String userId = securityContextUtils.getCurrentUserId();
                TokenUsage tokenUsage = TokenUsage.builder()
                        .userId(userId)
                        .request(requestType)
                        .tokenCount(response.getTokenUsage().getTotalTokens())
                        .model(response.getTokenUsage().getModel())
                        .provider(response.getTokenUsage().getProvider())
                        .build();
                tokenUsageApi.recordTokenUsage(tokenUsage);
                log.debug("Token usage saved - userId: {}, request: {}, tokens: {}, model: {}, provider: {}",
                        userId,
                        requestType,
                        response.getTokenUsage().getTotalTokens(),
                        response.getTokenUsage().getModel(),
                        response.getTokenUsage().getProvider());
            }
        } catch (Exception e) {
            log.warn("Failed to save token usage for request type: {}", requestType, e);
        }
    }
}
