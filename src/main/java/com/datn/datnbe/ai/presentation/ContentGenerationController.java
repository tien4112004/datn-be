package com.datn.datnbe.ai.presentation;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.datn.datnbe.ai.api.AIResultApi;
import com.datn.datnbe.ai.api.ContentGenerationApi;
import com.datn.datnbe.ai.dto.request.OutlinePromptRequest;
import com.datn.datnbe.ai.dto.request.PresentationPromptRequest;
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

@Slf4j
@RestController
@RequestMapping("api/")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class ContentGenerationController {
    ContentGenerationApi contentGenerationExternalApi;
    PresentationApi presentationApi;
    AIResultApi aiResultApi;
    static Integer OUTLINE_DELAY = 25; // milliseconds
    static Integer SLIDE_DELAY = 500; // milliseconds

    @PostMapping(value = "presentations/outline-generate", produces = MediaType.TEXT_PLAIN_VALUE)
    public Flux<String> generateOutline(@RequestBody OutlinePromptRequest request) {
        log.info("Received outline generation request: {}", request);

        StringBuilder result = new StringBuilder();

        // Create the background processing flux
        Flux<String> backgroundFlux = contentGenerationExternalApi.generateOutline(request)
                .delayElements(Duration.ofMillis(OUTLINE_DELAY))
                .doOnNext(chunk -> {
                    result.append(chunk);
                    log.info("Received outline chunk: {}", chunk);
                })
                .doOnComplete(() -> {
                    log.info("Outline generation completed. Full result: {}", result.toString());
                    // Save to database if needed
                })
                .doOnError(err -> log.error("Error generating outline", err))
                .onErrorResume(AppException.class, ex -> {
                    throw ex;
                })
                .cache();

        // Subscribe immediately to start background processing
        backgroundFlux.subscribe(chunk -> log.info("Background processing outline chunk"),
                error -> log.error("Background outline processing error", error),
                () -> log.info("Background outline processing completed"));

        // Return the same flux for the client
        return backgroundFlux.doOnSubscribe(subscription -> log.info("Client subscribed to outline generation stream"))
                .doOnCancel(() -> log.info("Client cancelled outline request, but background processing continues"));
    }

    @PostMapping(value = "presentations/generate", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<Flux<String>> generateSlides(@RequestBody PresentationPromptRequest request) {
        String presentationId = (new ObjectId()).toString();
        StringBuilder result = new StringBuilder();

        PresentationCreateRequest createRequest = PresentationCreateRequest.builder()
                .id(presentationId)
                .title("AI Generated Presentation")
                .slides(new ArrayList<>())
                .metadata(convertToMap(request.getPresentation()))
                .isParsed(false)
                .build();
        presentationApi.createPresentation(createRequest);

        // Create the background processing flux that runs independently
        Flux<String> backgroundFlux = contentGenerationExternalApi.generateSlides(request)
                .doOnNext(response -> log.info("Received response chunk: {}", response))
                .map(slide -> slide.substring("data: ".length()) + "\n\n")
                .delayElements(Duration.ofMillis(SLIDE_DELAY))
                .doOnNext(slide -> {
                    result.append(slide);
                    log.info("Processing slide in background: {}", slide);
                })
                .doOnComplete(() -> {
                    aiResultApi.saveAIResult(result.toString(), presentationId);
                    log.info("Slide generation completed, result saved with ID: {}", presentationId);
                })
                .doOnError(err -> log.error("Error generating slides for ID: {}", presentationId, err))
                .onErrorResume(err -> {
                    log.error("Error generating slides", err);
                    return Flux.error(new AppException(ErrorCode.GENERATION_ERROR,
                            "Failed to generate slides: " + err.getMessage()));
                })
                .cache(); // Cache the flux so it only executes once

        // Subscribe immediately to start background processing
        backgroundFlux.subscribe(slide -> log.info("Background processing: {}", slide),
                error -> log.error("Background processing error for ID: {}", presentationId, error),
                () -> log.info("Background processing completed for ID: {}", presentationId));

        // Return the same flux for the client
        var slideSse = backgroundFlux.doOnSubscribe(s -> log.info("Client subscribed to slide generation stream"))
                .doOnCancel(() -> log.info("Client cancelled request, but background processing continues for ID: {}",
                        presentationId));

        return ResponseEntity.ok().header("X-Presentation", presentationId).body(slideSse);
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
                .header("X-Presentation", presentationId)
                .body(AppResponseDto.<JsonNode>builder().data(data).build());
    }

    private Map<String, Object> convertToMap(Object object) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(object, Map.class);
    }
}
