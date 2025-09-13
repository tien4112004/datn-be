package com.datn.datnbe.ai.management;

import com.datn.datnbe.ai.api.AIResultApi;
import org.springframework.stereotype.Service;

import com.datn.datnbe.ai.api.ContentGenerationApi;
import com.datn.datnbe.ai.api.ModelSelectionApi;
import com.datn.datnbe.ai.config.chatmodelconfiguration.SystemPromptConfig;
import com.datn.datnbe.ai.dto.request.OutlinePromptRequest;
import com.datn.datnbe.ai.dto.request.PresentationPromptRequest;
import com.datn.datnbe.ai.factory.ChatClientFactory;
import com.datn.datnbe.ai.utils.MappingParamsUtils;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
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
public class ContentGenerationManagement implements ContentGenerationApi {
    SystemPromptConfig systemPromptConfig;
    ModelSelectionApi modelSelectionApi;
    ChatClientFactory chatClientFactory;
    AIResultApi aiResultApi;
    // AIEventPublisher aiEventPublisher;

    @Override
    public Flux<String> generateOutline(OutlinePromptRequest request) {

        if (!modelSelectionApi.isModelEnabled(request.getModel())) {
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

        if (!modelSelectionApi.isModelEnabled(request.getModel())) {
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
                .doOnError(error -> log.error("Error in streaming presentation generation: {}", error.getMessage()));
    }

    private String extractJsonFromResponse(String response) {
        String cleaned = response.trim();

        cleaned = cleaned.replaceAll("(?m)^```json\\s*|^```\\s*", "").replaceAll("\n---", ",").replaceAll(",\\s*$", "");

        cleaned = "{\"slides\":[" + cleaned + "]}";
        return cleaned;
    }
}
