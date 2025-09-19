package com.datn.datnbe.ai.presentation;

import java.time.Duration;
import java.util.ArrayList;

import com.datn.datnbe.sharedkernel.utils.JsonUtils;
import org.bson.types.ObjectId;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.datn.datnbe.ai.api.AIResultApi;
import com.datn.datnbe.ai.api.ContentGenerationApi;
import com.datn.datnbe.ai.dto.request.OutlinePromptRequest;
import com.datn.datnbe.ai.dto.request.PresentationPromptRequest;
import com.datn.datnbe.ai.utils.StreamingResponseUtils;
import com.datn.datnbe.document.api.PresentationApi;
import com.datn.datnbe.document.dto.request.PresentationCreateRequest;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;

@Slf4j
@RestController
@RequestMapping("api/")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class ContentGenerationController {
    ContentGenerationApi contentGenerationExternalApi;
    PresentationApi presentationApi;
    AIResultApi aiResultApi;
    ObjectMapper objectMapper;

    @PostMapping(value = "presentations/outline-generate", produces = MediaType.TEXT_PLAIN_VALUE)
    public Flux<String> generateOutline(@RequestBody OutlinePromptRequest request) {
        log.info("Received outline generation request: {}", request);

        return StreamingResponseUtils
                .streamWordByWordWithSpaces(10,
                        contentGenerationExternalApi.generateOutline(request)
                                .doOnSubscribe(subscription -> log.info("Starting outline generation stream")))
                .doOnError(error -> {
                    log.error("Error generating outline", error);
                    throw new AppException(ErrorCode.GENERATION_ERROR, "Failed to generate outline");
                })
                .onErrorMap(error -> new AppException(ErrorCode.GENERATION_ERROR,
                        "Failed to generate outline: " + error.getMessage()));
    }

    @PostMapping(value = "presentations/generate", produces = TEXT_EVENT_STREAM_VALUE)
    public Flux<String> generateSlidesV2(@RequestBody PresentationPromptRequest request) {
        return contentGenerationExternalApi.generateSlides(request)
                .doOnNext(response -> log.info("Received response chunk"))
                // Thử parse từng chunk ngay
                .flatMap(this::tryParseSlideFromChunk)
                .map(JsonUtils::toPrettyJsonSafe)
                .delayElements(Duration.ofMillis(800))
                .doOnSubscribe(s -> log.info("Starting slide generation stream"))
                .doOnNext(slide -> log.info("Streaming slide"))
                .doOnComplete(() -> log.info("Completed streaming all slides"))
                .onErrorMap(err -> {
                    log.error("Error generating slides", err);
                    return new AppException(ErrorCode.GENERATION_ERROR,
                            "Failed to generate slides: " + err.getMessage());
                });
    }

    private Flux<String> tryParseSlideFromChunk(String chunk) {
        try {
            // Nếu chunk chứa complete JSON object của 1 slide
            JsonNode node = objectMapper.readTree(chunk);
            if (node.isObject() && node.has("type") && node.has("data")) {
                return Flux.just(objectMapper.writeValueAsString(node));
            }

            // Nếu chunk chứa array slides hoàn chỉnh
            if (node.isObject() && node.has("slides")) {
                return JsonUtils.splitSlidesAsFlux(chunk);
            }

            // Skip chunk này nếu không parse được
            return Flux.empty();

        } catch (Exception e) {
            log.debug("Could not parse chunk as JSON, skipping: {}", chunk);
            return Flux.empty();
        }
    }

    @PostMapping(value = "presentations/generate/batch", produces = "application/json")
    public ResponseEntity<AppResponseDto<JsonNode>> generateSlidesBatch(
            @RequestBody PresentationPromptRequest request) {
        log.info("Received batch slide generation request: {}", request);
        String result;

        try {
            result = contentGenerationExternalApi.generateSlides(request)
                    .doOnSubscribe(subscription -> log.info("Starting batch slide generation"))
                    .collectList()
                    .map(list -> String.join("", list))
                    .block();

            log.info("Batch slide generation completed successfully");

        } catch (Exception error) {
            log.error("Error generating slides in batch mode", error);
            throw new AppException(ErrorCode.GENERATION_ERROR,
                    "Failed to generate slides in batch mode: " + error.getMessage());
        }
        String presentationId = (new ObjectId()).toString();
        aiResultApi.saveAIResult(result, presentationId);

        PresentationCreateRequest createRequest = PresentationCreateRequest.builder()
                .id(presentationId)
                .title("AI Generated Presentation")
                .slides(new ArrayList<>())
                .isParsed(false)
                .build();
        var newPresentation = presentationApi.createPresentation(createRequest);

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode data = mapper.createObjectNode();
        data.put("aiResult", result);
        data.set("presentation", mapper.valueToTree(newPresentation));

        return ResponseEntity.ok()
                .header("presentationId", presentationId)
                .body(AppResponseDto.<JsonNode>builder().data(data).build());
    }
}
