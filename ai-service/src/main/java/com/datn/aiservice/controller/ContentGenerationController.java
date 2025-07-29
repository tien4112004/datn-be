package com.datn.aiservice.controller;

import com.datn.aiservice.dto.request.OutlinePromptRequest;
import com.datn.aiservice.service.interfaces.SlideGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping("/api/content")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class ContentGenerationController {
    SlideGenerationService slideGenerationServiceImpl;

    @PostMapping(value = "/outline/generate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> generateOutline(@RequestBody OutlinePromptRequest request) {
        log.info("Received outline generation request: {}", request);

        return slideGenerationServiceImpl.generateOutline(request)
                .map(outlinePromptResource -> outlinePromptResource)
                .doOnNext(response -> log.info("Generated outline: {}", response))
                .doOnError(error -> log.error("Error generating outline", error));
    }
}
