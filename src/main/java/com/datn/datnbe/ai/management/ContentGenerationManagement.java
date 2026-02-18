package com.datn.datnbe.ai.management;

import com.datn.datnbe.ai.api.ContentGenerationApi;
import com.datn.datnbe.ai.api.ModelSelectionApi;
import com.datn.datnbe.ai.apiclient.AIApiClient;
import com.datn.datnbe.ai.dto.request.AIGatewayGenerateQuestionsRequest;
import com.datn.datnbe.ai.dto.request.GenerateQuestionsFromMatrixRequest;
import com.datn.datnbe.ai.dto.request.MindmapPromptRequest;
import com.datn.datnbe.ai.dto.request.OutlinePromptRequest;
import com.datn.datnbe.ai.dto.request.PresentationPromptRequest;
import com.datn.datnbe.ai.dto.response.GenerateQuestionsFromMatrixResponse;
import com.datn.datnbe.ai.dto.response.AiWokerResponse;
import com.datn.datnbe.ai.utils.MappingParamsUtils;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import com.datn.datnbe.document.entity.Chapter;
import com.datn.datnbe.document.dto.DimensionTopicDto;
import com.datn.datnbe.document.dto.AssignmentMatrixDto;
import com.datn.datnbe.document.dto.request.GenerateMatrixRequest;
import com.datn.datnbe.document.dto.request.GenerateQuestionsFromTopicRequest;
import com.datn.datnbe.document.repository.ChapterRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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
    ChapterRepository chapterRepository;

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

    @Value("${ai.api.questions-from-matrix-endpoint:/api/exams/generate-questions-from-matrix}")
    @NonFinal
    String QUESTIONS_FROM_MATRIX_ENDPOINT;

    @Override
    public Flux<String> generateOutline(OutlinePromptRequest request, String traceId) {

        if (!modelSelectionApi.isModelEnabled(request.getModel())) {
            log.error("Model {} is not enabled for outline generation", request.getModel());
            return Flux.error(new AppException(ErrorCode.MODEL_NOT_ENABLED));
        }

        log.info("Calling AI to stream outline generation with traceId: {}", traceId);

        Map<String, String> headers = new java.util.HashMap<>();
        headers.put("X-Trace-ID", traceId);
        headers.put("provider", request.getProvider());

        return aiApiClient.postSse(OUTLINE_API_ENDPOINT, MappingParamsUtils.constructParams(request), headers)
                .map(chunk -> new String(Base64.getDecoder().decode(chunk), StandardCharsets.UTF_8));
    }

    @Override
    public Flux<String> generateSlides(PresentationPromptRequest request, String traceId) {
        log.info("Starting streaming presentation generation for slides with traceId: {}", traceId);

        if (!modelSelectionApi.isModelEnabled(request.getModel())) {
            log.error("Model {} is not enabled for slide generation", request.getModel());
            return Flux.error(new AppException(ErrorCode.MODEL_NOT_ENABLED));
        }

        log.info("Calling AI to stream presentation slides");

        Map<String, String> headers = new java.util.HashMap<>();
        headers.put("X-Trace-ID", traceId);
        headers.put("provider", request.getProvider());

        return aiApiClient.postSse(PRESENTATION_API_ENDPOINT, MappingParamsUtils.constructParams(request), headers);
    }

    @Override
    public String generateOutlineBatch(OutlinePromptRequest request, String traceId) {
        log.info("Starting batch outline generation with traceId: {}", traceId);

        if (!modelSelectionApi.isModelEnabled(request.getModel())) {
            throw new AppException(ErrorCode.MODEL_NOT_ENABLED);
        }

        log.info("Calling AI to generate outline in batch mode");
        try {
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("X-Trace-ID", traceId);
            headers.set("provider", request.getProvider());

            AiWokerResponse response = aiApiClient.post(OUTLINE_BATCH_API_ENDPOINT,
                    MappingParamsUtils.constructParams(request),
                    AiWokerResponse.class,
                    headers);

            // Token usage will be extracted by controller via extractAndSaveTokenUsage(traceId)
            // Do not call saveTokenUsageIfPresent here - it doesn't have correct traceId

            log.info("Batch outline generation completed successfully");
            return response.getData();
        } catch (Exception e) {
            log.error("Error during batch outline generation", e);
            throw new AppException(ErrorCode.AI_WORKER_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    public String generateSlidesBatch(PresentationPromptRequest request, String traceId) {
        log.info("Starting batch presentation generation for slides with traceId: {}", traceId);

        if (!modelSelectionApi.isModelEnabled(request.getModel())) {
            log.error("Model {} is not enabled for slide generation", request.getModel());
            throw new AppException(ErrorCode.MODEL_NOT_ENABLED);
        }

        log.info("Calling AI to generate presentation slides in batch mode");
        try {
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("X-Trace-ID", traceId);
            headers.set("provider", request.getProvider());

            AiWokerResponse response = aiApiClient.post(PRESENTATION_BATCH_API_ENDPOINT,
                    MappingParamsUtils.constructParams(request),
                    AiWokerResponse.class,
                    headers);

            // Token usage will be extracted by controller via extractAndSaveTokenUsage(traceId)
            // Do not call saveTokenUsageIfPresent here - it doesn't have correct traceId

            log.info("Batch presentation generation completed successfully");
            return response.getData();
        } catch (Exception e) {
            log.error("Error during batch presentation generation", e);
            throw new AppException(ErrorCode.AI_WORKER_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    public String generateMindmap(MindmapPromptRequest request, String traceId) {
        log.info("Starting mindmap generation with traceId: {}", traceId);

        if (!modelSelectionApi.isModelEnabled(request.getModel())) {
            throw new AppException(ErrorCode.MODEL_NOT_ENABLED);
        }

        log.info("Calling AI to generate mindmap");
        try {
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("X-Trace-ID", traceId);
            headers.set("provider", request.getProvider());

            AiWokerResponse response = aiApiClient.post(MINDMAP_API_ENDPOINT,
                    MappingParamsUtils.constructParams(request),
                    AiWokerResponse.class,
                    headers);

            log.info("Mindmap generation completed successfully");
            return response.getData();
        } catch (Exception e) {
            log.error("Error during mindmap generation", e);
            throw new AppException(ErrorCode.AI_WORKER_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    public AssignmentMatrixDto generateAssignmentMatrix(GenerateMatrixRequest request, String traceId) {
        log.info("Starting exam matrix generation for grade: {}, subject: {}",
                request.getGrade(),
                request.getSubject());

        // TODO: Replace this mock implementation with actual chapter fetching from database
        // Query: SELECT chapter_name FROM chapters WHERE grade = ? AND subject = ?
        List<String> chapters = fetchChaptersFromDatabase(request.getGrade(), request.getSubject());

        // Create a new request with chapters included
        GenerateMatrixRequest requestWithChapters = GenerateMatrixRequest.builder()
                .name(request.getName())
                .chapters(chapters)
                .grade(request.getGrade())
                .subject(request.getSubject())
                .totalQuestions(request.getTotalQuestions())
                .totalPoints(request.getTotalPoints())
                .difficulties(request.getDifficulties())
                .questionTypes(request.getQuestionTypes())
                .prompt(request.getPrompt())
                .language(request.getLanguage())
                .provider(request.getProvider() != null ? request.getProvider().toLowerCase() : null)
                .model(request.getModel() != null ? request.getModel().toLowerCase() : null)
                .build();

        if (!modelSelectionApi.isModelEnabled(request.getModel())) {
            throw new AppException(ErrorCode.MODEL_NOT_ENABLED);
        }

        log.info("Calling AI to generate exam matrix via: {} with {} chapters",
                EXAM_MATRIX_API_ENDPOINT,
                chapters.size());
        try {
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("X-Trace-ID", traceId);
            AssignmentMatrixDto result = aiApiClient
                    .post(EXAM_MATRIX_API_ENDPOINT, requestWithChapters, AssignmentMatrixDto.class, headers);

            // Add numeric IDs to topics (backend processing)
            if (result.getDimensions() != null && result.getDimensions().getTopics() != null) {
                List<DimensionTopicDto> topics = result.getDimensions().getTopics();
                for (int i = 0; i < topics.size(); i++) {
                    topics.get(i).setId(String.valueOf(i + 1));
                }
            }

            log.info("Exam matrix generation completed successfully");
            return result;
        } catch (Exception e) {
            log.error("Error during exam matrix generation", e);
            throw new AppException(ErrorCode.AI_WORKER_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * Fetch chapters from database based on grade and subject.
     *
     * @param grade Grade level
     * @param subject Subject code (T, TV, TA)
     * @return List of chapter names
     */
    private List<String> fetchChaptersFromDatabase(String grade, String subject) {
        log.info("Fetching chapters from database for grade: {}, subject: {}", grade, subject);

        List<Chapter> chapters = chapterRepository.findAllByGradeAndSubject(grade, subject);

        if (chapters.isEmpty()) {
            log.warn("No chapters found for grade: {}, subject: {}. Using fallback.", grade, subject);
            // Fallback to generic chapters if none found
            return Arrays.asList("fallback-1|Chương 1: Kiến thức cơ bản",
                    "fallback-2|Chương 2: Kiến thức nâng cao",
                    "fallback-3|Chương 3: Ứng dụng thực tế");
        }

        // Return chapter data formatted as "id|name" for AI worker to parse
        List<String> chapterData = chapters.stream()
                .map(ch -> ch.getId() + "|" + ch.getName())
                .collect(Collectors.toList());

        log.info("Found {} chapters for grade: {}, subject: {}", chapterData.size(), grade, subject);
        return chapterData;
    }

    @Override
    public String generateQuestions(GenerateQuestionsFromTopicRequest request, String traceId) {
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

        AIGatewayGenerateQuestionsRequest aiRequest = AIGatewayGenerateQuestionsRequest.builder()
                .topic(request.getTopic())
                .grade(request.getGrade())
                .subject(request.getSubject())
                .questionsPerDifficulty(difficultyMap)
                .questionTypes(questionTypesList)
                .prompt(request.getPrompt())
                .provider(request.getProvider() != null ? request.getProvider().toLowerCase() : "google")
                .model(request.getModel() != null ? request.getModel().toLowerCase() : "gemini-2.5-flash-lite")
                .build();

        log.info("Calling GenAI-Gateway at endpoint: {}", QUESTIONS_API_ENDPOINT);
        try {
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("X-Trace-ID", traceId);
            String jsonResponse = aiApiClient.post(QUESTIONS_API_ENDPOINT, aiRequest, String.class, headers);

            log.info("Successfully received GenAI-Gateway response");
            return jsonResponse;
        } catch (Exception e) {
            log.error("Error during question generation", e);
            throw new AppException(ErrorCode.AI_WORKER_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    public GenerateQuestionsFromMatrixResponse generateQuestionsFromMatrix(GenerateQuestionsFromMatrixRequest request,
            String traceId) {
        log.info("Generating questions from matrix for grade: {}, subject: {}, topics: {}",
                request.getGrade(),
                request.getSubject(),
                request.getTopics() != null ? request.getTopics().size() : 0);

        log.info("Calling GenAI-Gateway at endpoint: {}", QUESTIONS_FROM_MATRIX_ENDPOINT);
        try {
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("X-Trace-ID", traceId);

            // GenAI Gateway returns raw JSON string - we handle parsing in backend
            String rawJsonResponse = aiApiClient.post(QUESTIONS_FROM_MATRIX_ENDPOINT, request, String.class, headers);

            log.info("Successfully received raw JSON response from GenAI-Gateway");

            // Return raw response wrapped in DTO for further processing by caller
            // The AssignmentManagement will handle parsing, field filling, and grouping
            GenerateQuestionsFromMatrixResponse response = new GenerateQuestionsFromMatrixResponse();
            response.setRawJson(rawJsonResponse);
            return response;
        } catch (Exception e) {
            log.error("Error during matrix-based question generation", e);
            throw new AppException(ErrorCode.AI_WORKER_SERVER_ERROR, e.getMessage());
        }
    }

}
