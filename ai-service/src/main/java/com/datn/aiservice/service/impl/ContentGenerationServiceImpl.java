package com.datn.aiservice.service.impl;

import com.datn.aiservice.config.chatmodelconfiguration.SystemPromptConfig;
import com.datn.aiservice.dto.request.OutlinePromptRequest;
import com.datn.aiservice.dto.request.PresentationPromptRequest;
import com.datn.aiservice.dto.response.PresentationResponse;
import com.datn.aiservice.event.PresentationGeneratedEvent;
import com.datn.aiservice.exceptions.AppException;
import com.datn.aiservice.exceptions.ErrorCode;
import com.datn.aiservice.factory.ChatClientFactory;
import com.datn.aiservice.messaging.AIEventPublisher;
import com.datn.aiservice.service.interfaces.ContentGenerationService;
import com.datn.aiservice.service.interfaces.ModelSelectionService;
import com.datn.aiservice.utils.MappingParamsUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ContentGenerationServiceImpl implements ContentGenerationService {
    SystemPromptConfig systemPromptConfig;
    ModelSelectionService modelSelectionService;
    ChatClientFactory chatClientFactory;
    AIEventPublisher aiEventPublisher;
    ObjectMapper objectMapper;

    @Override
    public Flux<String> generateOutline(OutlinePromptRequest request) {

        if (!modelSelectionService.isModelEnabled(request.getModel())) {
            log.error("Model {} is not enabled for outline generation", request.getModel());
            return Flux.error(new AppException(ErrorCode.MODEL_NOT_ENABLED));
        }
        var chatClient = chatClientFactory.getChatClient(request.getModel());

        return chatClient.prompt()
                .user(prompt -> prompt.text(systemPromptConfig.getOutlinePrompt())
                        .params(MappingParamsUtils.constructParams(request)))
                .stream()
                .content();
    }

    @Override
    public Flux<String> generateSlides(PresentationPromptRequest request) {
        log.info("Starting streaming presentation generation for slides");

        if (!modelSelectionService.isModelEnabled(request.getModel())) {
            log.error("Model {} is not enabled for slide generation", request.getModel());
            return Flux.error(new AppException(ErrorCode.MODEL_NOT_ENABLED));
        }

        var chatClient = chatClientFactory.getChatClient(request.getModel());

        log.info("Calling AI to stream presentation slides");

        StringBuilder completeResponse = new StringBuilder();

        return chatClient.prompt()
                .user(promptSys -> promptSys.text(systemPromptConfig.getSlidePrompt())
                        .params(MappingParamsUtils.constructParams(request)))
                .stream()
                .content()
                .doOnNext(chunk -> completeResponse.append(chunk))
                .doOnComplete(() -> {
                    log.info("Streaming presentation generation completed");
                    String cleanedJson = extractJsonFromResponse(completeResponse.toString());
                    try {
                        // Parse the complete JSON response
                        PresentationResponse slideResponse = objectMapper.readValue(cleanedJson,
                                PresentationResponse.class);

                        // Publish event with actual slides
                        var event = new PresentationGeneratedEvent(slideResponse.getSlides());
                        aiEventPublisher.publishEvent(event);
                    } catch (JsonProcessingException e) {
                        log.error("Error parsing JSON response: {}", e.getMessage());
                        throw new AppException(ErrorCode.JSON_PARSING_ERROR);
                    }
                })
                .doOnError(error -> log.error("Error in streaming presentation generation: {}", error.getMessage()));
    }

    private String extractJsonFromResponse(String response) {
        // Remove markdown code blocks
        String cleaned = response.trim();

        cleaned = cleaned.replaceAll("(?m)^```json\\s*|^```\\s*", "").replaceAll("\n---", ",").replaceAll(",\\s*$", "");

        cleaned = "{\"slides\":[" + cleaned + "]}";
        return cleaned;
    }
}
