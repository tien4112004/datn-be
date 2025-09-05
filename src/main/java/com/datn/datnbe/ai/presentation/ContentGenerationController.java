package com.datn.datnbe.ai.presentation;

import com.datn.datnbe.document.api.PresentationApi;
import com.datn.datnbe.document.dto.request.PresentationCreateRequest;
import org.bson.types.ObjectId;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.datn.datnbe.ai.api.ContentGenerationApi;
import com.datn.datnbe.ai.dto.request.OutlinePromptRequest;
import com.datn.datnbe.ai.dto.request.PresentationPromptRequest;
import com.datn.datnbe.ai.utils.StreamingResponseUtils;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.ArrayList;

@Slf4j
@RestController
@RequestMapping("api/")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class ContentGenerationController {
    ContentGenerationApi contentGenerationExternalApi;
    PresentationApi presentationApi;

    @PostMapping(value = "presentations/outline-generate", produces = MediaType.TEXT_PLAIN_VALUE)
    public Flux<String> generateOutline(@RequestBody OutlinePromptRequest request) {
        log.info("Received outline generation request: {}", request);

        return StreamingResponseUtils
                .streamWordByWordWithSpaces(StreamingResponseUtils.X_DELAY,
                        contentGenerationExternalApi.generateOutline(request)
                                .doOnSubscribe(subscription -> log.info("Starting outline generation stream")))
                .doOnError(error -> {
                    log.error("Error generating outline", error);
                    throw new AppException(ErrorCode.GENERATION_ERROR, "Failed to generate outline");
                })
                .onErrorMap(error -> new AppException(ErrorCode.GENERATION_ERROR,
                        "Failed to generate outline: " + error.getMessage()));
    }

    @PostMapping(value = "presentations/generate", produces = MediaType.TEXT_PLAIN_VALUE)
    public Flux<String> generateSlides(@RequestBody PresentationPromptRequest request) {
        return StreamingResponseUtils
            .streamByJsonObject(StreamingResponseUtils.HIGH_DELAY,
                    contentGenerationExternalApi.generateSlides(request)
                                .doOnSubscribe(subscription -> log.info("Starting slide generation stream")))
            .doOnError(error -> {
                log.error("Error generating slides", error);
                throw new AppException(ErrorCode.GENERATION_ERROR, "Failed to generate slides");
            })
            .onErrorMap(error -> new AppException(ErrorCode.GENERATION_ERROR,
                    "Failed to generate slides: " + error.getMessage()));
    }

    @PostMapping(value = "presentations/generate/batch", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> generateSlidesBatch(@RequestBody PresentationPromptRequest request) {
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
        contentGenerationExternalApi.saveAIResult(result, presentationId);

        PresentationCreateRequest createRequest = PresentationCreateRequest.builder()
                .id(presentationId)
                .title("AI Generated Presentation")
                .slides(new ArrayList<>())
                .build();

        presentationApi.createPresentation(createRequest);

        return ResponseEntity.ok()
                .header("presentationId",presentationId)
                .body(result);
    }
}
