package com.datn.aiservice.controller;

import com.datn.aiservice.dto.request.OutlinePromptRequest;
import com.datn.aiservice.dto.request.SlidePromptRequest;
import com.datn.aiservice.service.interfaces.ContentGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class ContentGenerationController {
    ContentGenerationService contentGenerationServiceImpl;

    @PostMapping(value = "/presentations/outline-generate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> generateOutline(@RequestBody OutlinePromptRequest request) {
        log.info("Received outline generation request: {}", request);

        return contentGenerationServiceImpl.generateOutline(request)
                .bufferUntil(token -> token.contains("\n"))
                .map(bufferedToken -> {
                            String combined = String.join("", bufferedToken);
                            combined = combined.replace("\n", "");
                            return combined;
                        }
                )
                .doOnNext(response -> log.info("Generated outline: {}", response))
                .doOnError(error -> log.error("Error generating outline", error));
    }

    @PostMapping(value = "/presentations/generate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> generateSlides(@RequestBody SlidePromptRequest request) {
        log.info("Received slide generation request: {}", request);
        return contentGenerationServiceImpl.generateSlides(request)
                .bufferUntil(token -> token.contains("\n"))
                .map(bufferedToken -> {
                            String combined = String.join("", bufferedToken);
                            combined = combined.replace("\n", "");
                            return combined;
                        }
                )
                .doOnNext(response -> log.info("{}", response))
                .doOnError(error -> log.error("Error generating slides", error));
    }
}
