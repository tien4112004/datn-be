package com.datn.aiservice.service.impl;

import com.datn.aiservice.dto.request.OutlinePromptRequest;
import com.datn.aiservice.dto.request.SlidePromptRequest;
import com.datn.aiservice.exceptions.AppException;
import com.datn.aiservice.exceptions.ErrorCode;
import com.datn.aiservice.factory.ChatClientFactory;
import com.datn.aiservice.service.interfaces.ContentGenerationService;
import com.datn.aiservice.service.interfaces.ModelSelectionService;
import com.datn.aiservice.utils.MappingParamsUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
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
        log.info("Starting streaming presentation generation for outline");
        
        try {
            // For now, use a default model (this should be made configurable)
            ChatClient chatClient = chatClientFactory.getChatClient(request.getModel());
            
            // Create parameters for the template
            Map<String, Object> templateParams = Map.of(
                "outline", request.getOutline(),
                "language", "English",
                "style", "professional"
            );
            
            log.info("Calling AI to stream presentation slides");
            
            // Call the AI model with streaming using the same pattern as outline generation
            return chatClient.prompt()
                    .system(promptSys -> promptSys.text(slidePromptResource)
                            .params(templateParams))
                    .stream().content()
                    .doOnNext(token -> log.debug("Received token: {}", token))
                    .doOnComplete(() -> log.info("Streaming presentation generation completed"))
                    .doOnError(error -> log.error("Error in streaming presentation generation: {}", error.getMessage()));
            
        } catch (Exception e) {
            log.error("Error in streaming presentation generation setup: {}", e.getMessage(), e);
            return Flux.error(new RuntimeException("Failed to setup streaming presentation generation", e));
        }
    }
}
