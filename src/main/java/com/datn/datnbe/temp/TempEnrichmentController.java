package com.datn.datnbe.temp;

import com.datn.datnbe.ai.api.ImageGenerationApi;
import com.datn.datnbe.ai.apiclient.AIApiClient;
import com.datn.datnbe.ai.dto.request.ImagePromptRequest;
import com.datn.datnbe.document.api.MediaStorageApi;
import com.datn.datnbe.document.dto.response.UploadedMediaResponseDto;
import com.datn.datnbe.document.entity.Question;
import com.datn.datnbe.document.entity.QuestionBankItem;
import com.datn.datnbe.document.mapper.QuestionEntityMapper;
import com.datn.datnbe.document.repository.QuestionRepository;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import com.datn.datnbe.temp.dto.EnrichQuestionsRequest;
import com.datn.datnbe.temp.dto.EnrichmentResultDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * TEMPORARY controller for enriching the question database with image-based questions.
 *
 * <p>Remove this entire file (and the temp package) once enrichment is complete.
 * To disable without deleting: remove {@code .requestMatchers("/api/temp/**").permitAll()}
 * from SecurityConfig.
 *
 * <p>Endpoint: {@code POST /api/temp/enrich-questions}
 *
 * <p>Flow:
 * <ol>
 *   <li>Call GenAI-Gateway {@code /api/temp/questions/generate-visual} — gets questions
 *       where each option (MULTIPLE_CHOICE) or left-side pair (MATCHING) carries an
 *       {@code imagePrompt} field.</li>
 *   <li>For each option/pair: call the image-generation API, upload the result, and set
 *       {@code imageUrl} (or {@code leftImageUrl}) directly on the JSON node.</li>
 *   <li>If at least one image was generated, save the question; otherwise discard it.</li>
 * </ol>
 */
@RestController
@RequestMapping("/api/temp")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class TempEnrichmentController {

    static final String SYSTEM_OWNER_ID = "00000000-0000-0000-0000-000000000001";
    static final String VISUAL_QUESTIONS_ENDPOINT = "/api/temp/questions/generate-visual";

    static final String DEFAULT_PROVIDER = "google";
    static final String DEFAULT_MODEL = "gemini-2.5-flash";
    static final String DEFAULT_IMAGE_MODEL = "imagen-3.0-generate-002";
    static final String DEFAULT_IMAGE_PROVIDER = "google";
    static final String DEFAULT_ASPECT_RATIO = "1:1";

    AIApiClient aiApiClient;
    ImageGenerationApi imageGenerationApi;
    MediaStorageApi mediaStorageApi;
    QuestionRepository questionRepository;
    QuestionEntityMapper questionEntityMapper;
    ObjectMapper objectMapper;

    @PostMapping("/enrich-questions")
    public ResponseEntity<AppResponseDto<EnrichmentResultDto>> enrichQuestionsWithImages(
            @Valid @RequestBody EnrichQuestionsRequest request) {

        log.info("[TEMP] Enrich request — grade={}, subject={}, chapter={}",
                request.getGrade(),
                request.getSubject(),
                request.getChapter());

        // ── 1. Generate visual questions from GenAI-Gateway ──────────────────
        String traceId = UUID.randomUUID().toString();

        String jsonResult;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Trace-ID", traceId);
            jsonResult = aiApiClient
                    .post(VISUAL_QUESTIONS_ENDPOINT, buildGatewayRequest(request), String.class, headers);
            log.info("[TEMP] GenAI-Gateway responded ({} chars)", jsonResult != null ? jsonResult.length() : 0);
        } catch (Exception e) {
            log.error("[TEMP] GenAI-Gateway call failed: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(AppResponseDto.<EnrichmentResultDto>builder()
                            .success(false)
                            .message("GenAI-Gateway call failed: " + e.getMessage())
                            .build());
        }

        // ── 2. Parse response as mutable JsonNode list ────────────────────────
        List<JsonNode> rawNodes;
        try {
            rawNodes = objectMapper.readValue(jsonResult, new TypeReference<>() {
            });
        } catch (Exception e) {
            log.error("[TEMP] Failed to parse GenAI response: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(AppResponseDto.<EnrichmentResultDto>builder()
                            .success(false)
                            .message("Failed to parse GenAI response: " + e.getMessage())
                            .build());
        }

        ObjectMapper lenient = objectMapper.copy()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

        // ── 3. Per-question: generate images per option/pair → save if ≥1 image
        List<String> savedIds = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int totalSaved = 0;
        int totalDiscarded = 0;
        int totalImagesGenerated = 0;
        int totalImagesFailed = 0;

        for (int i = 0; i < rawNodes.size(); i++) {
            JsonNode node = rawNodes.get(i);
            String type = node.path("type").asText("MULTIPLE_CHOICE");
            int optionImages = 0;

            if ("MULTIPLE_CHOICE".equals(type)) {
                JsonNode optionsNode = node.path("data").path("options");
                if (optionsNode.isArray()) {
                    ArrayNode options = (ArrayNode) optionsNode;
                    for (int j = 0; j < options.size(); j++) {
                        JsonNode optNode = options.get(j);
                        if (!optNode.isObject())
                            continue;
                        String imgPrompt = optNode.path("imagePrompt").asText(null);
                        if (imgPrompt == null || imgPrompt.isBlank())
                            continue;

                        try {
                            String cdnUrl = generateAndUploadImage(imgPrompt, request, traceId + "_q" + i + "_opt" + j);
                            ((ObjectNode) optNode).put("imageUrl", cdnUrl);
                            optionImages++;
                            totalImagesGenerated++;
                            log.info("[TEMP] Q{}/opt{}: image attached — {}", i, j, cdnUrl);
                        } catch (Exception e) {
                            totalImagesFailed++;
                            String msg = "Q" + i + "/opt" + j + ": image failed — " + e.getMessage();
                            log.warn("[TEMP] {}", msg);
                            errors.add(msg);
                        }
                    }
                }

            } else if ("MATCHING".equals(type)) {
                JsonNode pairsNode = node.path("data").path("pairs");
                if (pairsNode.isArray()) {
                    ArrayNode pairs = (ArrayNode) pairsNode;
                    for (int j = 0; j < pairs.size(); j++) {
                        JsonNode pairNode = pairs.get(j);
                        if (!pairNode.isObject())
                            continue;
                        ObjectNode pairObj = (ObjectNode) pairNode;

                        String leftPrompt = pairNode.path("leftImagePrompt").asText(null);
                        if (leftPrompt != null && !leftPrompt.isBlank()) {
                            try {
                                String cdnUrl = generateAndUploadImage(leftPrompt,
                                        request,
                                        traceId + "_q" + i + "_pair" + j + "L");
                                pairObj.put("leftImageUrl", cdnUrl);
                                optionImages++;
                                totalImagesGenerated++;
                                log.info("[TEMP] Q{}/pair{}/left: image attached — {}", i, j, cdnUrl);
                            } catch (Exception e) {
                                totalImagesFailed++;
                                String msg = "Q" + i + "/pair" + j + "/left: image failed — " + e.getMessage();
                                log.warn("[TEMP] {}", msg);
                                errors.add(msg);
                            }
                        }

                        String rightPrompt = pairNode.path("rightImagePrompt").asText(null);
                        if (rightPrompt != null && !rightPrompt.isBlank()) {
                            try {
                                String cdnUrl = generateAndUploadImage(rightPrompt,
                                        request,
                                        traceId + "_q" + i + "_pair" + j + "R");
                                pairObj.put("rightImageUrl", cdnUrl);
                                optionImages++;
                                totalImagesGenerated++;
                                log.info("[TEMP] Q{}/pair{}/right: image attached — {}", i, j, cdnUrl);
                            } catch (Exception e) {
                                totalImagesFailed++;
                                String msg = "Q" + i + "/pair" + j + "/right: image failed — " + e.getMessage();
                                log.warn("[TEMP] {}", msg);
                                errors.add(msg);
                            }
                        }
                    }
                }
            }

            // ── Discard if no images were generated ───────────────────────────
            if (optionImages == 0) {
                totalDiscarded++;
                String msg = "Question " + i + " (type=" + type + "): no images generated — discarded";
                log.warn("[TEMP] {}", msg);
                errors.add(msg);
                continue;
            }

            // ── Parse and save ─────────────────────────────────────────────────
            Question aiQuestion;
            try {
                aiQuestion = lenient.treeToValue(node, Question.class);
            } catch (Exception e) {
                totalDiscarded++;
                String msg = "Question " + i + ": parse error — " + e.getMessage();
                log.warn("[TEMP] {}", msg);
                errors.add(msg);
                continue;
            }

            QuestionBankItem entity = buildEntity(aiQuestion);
            try {
                QuestionBankItem saved = questionRepository.save(entity);
                savedIds.add(saved.getId());
                totalSaved++;
                log.info("[TEMP] Q{}: saved with {} image(s) — id={}", i, optionImages, saved.getId());
            } catch (Exception e) {
                totalDiscarded++;
                String msg = "Question " + i + ": DB save failed — " + e.getMessage();
                log.error("[TEMP] {}", msg);
                errors.add(msg);
            }
        }

        log.info("[TEMP] Done — generated={}, saved={}, discarded={}, imagesOk={}, imagesFailed={}",
                rawNodes.size(),
                totalSaved,
                totalDiscarded,
                totalImagesGenerated,
                totalImagesFailed);

        return ResponseEntity.ok(AppResponseDto.success(EnrichmentResultDto.builder()
                .totalGenerated(rawNodes.size())
                .totalSaved(totalSaved)
                .totalDiscarded(totalDiscarded)
                .totalImagesGenerated(totalImagesGenerated)
                .totalImagesFailed(totalImagesFailed)
                .savedQuestionIds(savedIds)
                .errors(errors)
                .build()));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Map<String, Object> buildGatewayRequest(EnrichQuestionsRequest req) {
        Map<String, Object> body = new HashMap<>();
        body.put("grade", req.getGrade());
        body.put("subject", req.getSubject());
        body.put("chapter", req.getChapter());
        body.put("questions_per_difficulty", req.getQuestionsPerDifficulty());
        body.put("question_types",
                req.getQuestionTypes() != null ? req.getQuestionTypes() : List.of("MULTIPLE_CHOICE"));
        body.put("provider", req.getProvider() != null ? req.getProvider() : DEFAULT_PROVIDER);
        body.put("model", req.getModel() != null ? req.getModel() : DEFAULT_MODEL);
        if (req.getPrompt() != null) {
            body.put("prompt", req.getPrompt());
        }
        return body;
    }

    private String generateAndUploadImage(String imagePrompt, EnrichQuestionsRequest req, String traceId) {
        ImagePromptRequest imgRequest = ImagePromptRequest.builder()
                .prompt(imagePrompt)
                .model(req.getImageModel() != null ? req.getImageModel() : DEFAULT_IMAGE_MODEL)
                .provider(req.getImageProvider() != null ? req.getImageProvider() : DEFAULT_IMAGE_PROVIDER)
                .aspectRatio(req.getImageAspectRatio() != null ? req.getImageAspectRatio() : DEFAULT_ASPECT_RATIO)
                .build();

        List<MultipartFile> images = imageGenerationApi.generateImage(imgRequest, traceId);

        if (images == null || images.isEmpty()) {
            throw new IllegalStateException("Image generation returned no images");
        }

        UploadedMediaResponseDto uploaded = mediaStorageApi.upload(images.get(0), SYSTEM_OWNER_ID);
        return uploaded.getCdnUrl();
    }

    private QuestionBankItem buildEntity(Question q) {
        Object convertedData = questionEntityMapper.convertDataToQuestionData(q.getData(),
                q.getType() != null ? q.getType().name() : "MULTIPLE_CHOICE");

        return QuestionBankItem.builder()
                .type(q.getType())
                .difficulty(q.getDifficulty())
                .title(q.getTitle())
                .titleImageUrl(q.getTitleImageUrl())
                .explanation(q.getExplanation())
                .grade(q.getGrade())
                .chapter(q.getChapter())
                .subject(q.getSubject())
                .data(convertedData)
                .ownerId(SYSTEM_OWNER_ID)
                .build();
    }
}
