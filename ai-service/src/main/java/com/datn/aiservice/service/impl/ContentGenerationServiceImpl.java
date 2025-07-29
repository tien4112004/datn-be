package com.datn.aiservice.service.impl;

import com.datn.aiservice.dto.request.OutlinePromptRequest;
import com.datn.aiservice.dto.request.SlidePromptRequest;
import com.datn.aiservice.exceptions.AppException;
import com.datn.aiservice.exceptions.ErrorCode;
import com.datn.aiservice.factory.ChatClientFactory;
import com.datn.aiservice.service.interfaces.ModelSelectionService;
import com.datn.aiservice.service.interfaces.ContentGenerationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Map;

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
        log.info("Using chat client: {}", chatClient.getClass().getSimpleName());

        return chatClient.prompt()
                .user(promptUserSpec -> promptUserSpec.text(outlinePromptResource)
                        .params(Map.of(
                                "language", request.getLanguage(),
                                "topic", request.getTopic(),
                                "slide_count", request.getSlideCount(),
                                "learning_objective", request.getLearningObjective(),
                                "target_age", request.getTargetAge())))
                .stream().content();
    }

    @Override
    public Flux<String> generateSlides(SlidePromptRequest request) {
        throw new UnsupportedOperationException("Slide generation is not supported yet.");
    }
}
