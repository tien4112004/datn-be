package com.datn.datnbe.ai.presentation;

import java.time.Duration;
import java.util.ArrayList;

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

        //        return StreamingResponseUtils
        //                .streamWordByWordWithSpaces(10,
        //                        contentGenerationExternalApi.generateOutline(request)
        //                                .doOnSubscribe(subscription -> log.info("Starting outline generation stream")))
        //                .doOnError(error -> {
        //                    log.error("Error generating outline", error);
        //                    throw new AppException(ErrorCode.GENERATION_ERROR, "Failed to generate outline");
        //                })
        //                .onErrorMap(error -> new AppException(ErrorCode.GENERATION_ERROR,
        //                        "Failed to generate outline: " + error.getMessage()));

        return contentGenerationExternalApi.generateOutline(request)
                .delayElements(Duration.ofMillis(OUTLINE_DELAY))
                .doOnNext(chunk -> log.info("Received outline chunk: {}", chunk))
                .doOnSubscribe(subscription -> log.info("Starting outline generation stream"))
                .doOnError(error -> {
                    log.error("Error generating outline", error);
                    throw new AppException(ErrorCode.GENERATION_ERROR, "Failed to generate outline");
                })
                .onErrorMap(error -> new AppException(ErrorCode.GENERATION_ERROR,
                        "Failed to generate outline: " + error.getMessage()));
    }

    @PostMapping(value = "presentations/generate", produces = MediaType.TEXT_PLAIN_VALUE)
    public Flux<String> generateSlides(@RequestBody PresentationPromptRequest request) {
        String presentationId = (new ObjectId()).toString();
        StringBuilder result = new StringBuilder();

        PresentationCreateRequest createRequest = PresentationCreateRequest.builder()
                .id(presentationId)
                .title("AI Generated Presentation")
                .slides(new ArrayList<>())
                .isParsed(false)
                .build();
        presentationApi.createPresentation(createRequest);

        return contentGenerationExternalApi.generateSlides(request)
                .doOnNext(response -> log.info("Received response chunk: {}", response))
                .map(slide -> "```json" + slide.substring("data: ".length()) + "```\n\n")
                .delayElements(Duration.ofMillis(SLIDE_DELAY))
                .doOnSubscribe(s -> log.info("Starting slide generation stream"))
                .doOnNext(slide -> result.append(slide.substring("data: ".length())))
                .doOnComplete(() -> {
                    aiResultApi.saveAIResult(result.toString(), presentationId);
                    log.info("Slide generation completed, result saved with ID: {}", presentationId);
                })
                .onErrorMap(err -> {
                    log.error("Error generating slides", err);
                    return new AppException(ErrorCode.GENERATION_ERROR,
                            "Failed to generate slides: " + err.getMessage());
                });
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
