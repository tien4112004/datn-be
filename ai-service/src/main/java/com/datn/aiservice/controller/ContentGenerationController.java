package com.datn.aiservice.controller;

import com.datn.aiservice.dto.request.OutlinePromptRequest;
import com.datn.aiservice.service.interfaces.SlideGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping("/api/slides")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class ContentGenerationController {
    SlideGenerationService slideGenerationServiceImpl;
    // ICommandHandler commandHandler;

    @PostMapping(value = "/generate-outline", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> generateOutline(@RequestBody OutlinePromptRequest request) {
        log.info("Received outline generation request: {}", request);

        return slideGenerationServiceImpl.generateOutline(request)
                .map(token -> "data: " + token + "\n\n") // SSE format
                .doOnNext(data -> log.debug("Streaming: {}", data.trim()))
                .doOnComplete(() -> {
                    log.info("Streaming completed");

                    // Publish Event
                    // commandHandler.publishEvent(new OutlineGeneratedEvent(request.getTopic(), request.getLanguage()));
                })
                .doOnError(error -> log.error("Streaming error: {}", error.getMessage()));
    }
}
