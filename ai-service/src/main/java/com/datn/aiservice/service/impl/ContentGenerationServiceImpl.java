package com.datn.aiservice.service.impl;

import com.datn.aiservice.dto.request.OutlinePromptRequest;
import com.datn.aiservice.dto.request.SlidePromptRequest;
import com.datn.aiservice.dto.response.PresentationResponse;
import com.datn.aiservice.event.PresentationGeneratedEvent;
import com.datn.aiservice.exceptions.AppException;
import com.datn.aiservice.exceptions.ErrorCode;
import com.datn.aiservice.factory.ChatClientFactory;
import com.datn.aiservice.service.interfaces.ContentGenerationService;
import com.datn.aiservice.service.interfaces.ModelSelectionService;
import com.datn.aiservice.utils.MappingParamsUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ContentGenerationServiceImpl implements ContentGenerationService {
    Resource outlinePromptResource;
    Resource slidePromptResource;
    ModelSelectionService modelSelectionService;
    ChatClientFactory chatClientFactory;
    ApplicationEventPublisher applicationEventPublisher;
    ObjectMapper objectMapper;

    @Override
    public Flux<String> generateOutline(OutlinePromptRequest request) {


        if (!modelSelectionService.isModelEnabled(request.getModel())) {
            log.error("Model {} is not enabled for outline generation", request.getModel());
            return Flux.error(new AppException(ErrorCode.MODEL_NOT_ENABLED));
        }
        var chatClient = chatClientFactory.getChatClient(request.getModel());

        return chatClient.prompt()
                .user(prompt -> prompt.text(outlinePromptResource)
                        .params(MappingParamsUtils.constructParams(request))).stream().content();
    }

    @Override
    public Flux<String> generateSlides(SlidePromptRequest request) {
        log.info("Starting streaming presentation generation for slides");

        if (!modelSelectionService.isModelEnabled(request.getModel())) {
            log.error("Model {} is not enabled for slide generation", request.getModel());
            return Flux.error(new AppException(ErrorCode.MODEL_NOT_ENABLED));
        }

        var chatClient = chatClientFactory.getChatClient(request.getModel());
        
        log.info("Calling AI to stream presentation slides");
        
        StringBuilder completeResponse = new StringBuilder();

        return chatClient.prompt()
                .system(promptSys -> promptSys.text(slidePromptResource)
                        .params(MappingParamsUtils.constructParams(request)))
                .stream().content()
                .doOnNext(chunk -> completeResponse.append(chunk))
                .doOnComplete(() -> {
                    log.info("Streaming presentation generation completed");
                     try {
                        // Clean the response to extract JSON
                        String cleanedJson = extractJsonFromResponse(completeResponse.toString());
                        log.debug("Cleaned JSON response: {}", cleanedJson);
                        
                        // Parse the complete JSON response
                        PresentationResponse slideResponse = objectMapper.readValue(
                            cleanedJson, PresentationResponse.class);

                        // Publish event with actual slides
                        var event = new PresentationGeneratedEvent(slideResponse.getSlides());
                        applicationEventPublisher.publishEvent(event);

                    } catch (Exception e) {
                        log.error("Failed to parse slides from LLM response: {}", e.getMessage());
                        log.error("Raw response: {}", completeResponse.toString());
                    }
                })
                .doOnError(error -> log.error("Error in streaming presentation generation: {}", error.getMessage()));
    }

    private String extractJsonFromResponse(String response) {
    // Remove markdown code blocks
    String cleaned = response.trim();
    
    // Remove ```json and ``` if present
    if (cleaned.startsWith("```json")) {
        cleaned = cleaned.substring(7);
    } else if (cleaned.startsWith("```")) {
        cleaned = cleaned.substring(3);
    }
    
    if (cleaned.endsWith("```")) {
        cleaned = cleaned.substring(0, cleaned.length() - 3);
    }
    
    return cleaned.trim();
}
}
