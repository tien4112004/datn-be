package com.datn.aiservice.service.impl;

import com.datn.aiservice.dto.request.OutlinePromptRequest;
import com.datn.aiservice.dto.request.SlidePromptRequest;
import com.datn.aiservice.factory.ChatClientFactory;
import com.datn.aiservice.service.interfaces.ModelSelectionService;
import com.datn.aiservice.service.interfaces.SlideGenerationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class SlideGenerationServiceImpl implements SlideGenerationService {
    Resource outlinePromptResource;
    Resource slidePromptResource;
    ModelSelectionService modelSelectionService;
    ChatClientFactory chatClientFactory;

    @Override
    public Flux<String> generateOutline(OutlinePromptRequest request) {
        if (!modelSelectionService.isModelEnabled(request.getModel())) {
            // Log the error and throw an exception
        }

        // Use the chat client factory to get the appropriate chat client
        // and generate the outline based on the request.
        var chatClient = chatClientFactory.getChatClient(request.getModel());

        throw new UnsupportedOperationException("Outline generation is not supported yet.");
    }

    @Override
    public Flux<String> generateSlides(SlidePromptRequest request) {
        throw new UnsupportedOperationException("Slide generation is not supported yet.");
    }
}
