package com.datn.datnbe.ai.management;

import org.springframework.stereotype.Service;

import com.datn.datnbe.ai.api.AIResultApi;
import com.datn.datnbe.ai.api.ContentGenerationApi;
import com.datn.datnbe.ai.api.ModelSelectionApi;
import com.datn.datnbe.ai.apiclient.AIApiClient;
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
    AIApiClient aiApiClient;
    // AIEventPublisher aiEventPublisher;

    @Override
    public Flux<String> generateOutline(OutlinePromptRequest request) {

        if (!modelSelectionApi.isModelEnabled(request.getModel())) {
            log.error("Model {} is not enabled for outline generation", request.getModel());
            return Flux.error(new AppException(ErrorCode.MODEL_NOT_ENABLED));
        }
        var chatClient = chatClientFactory.getChatClient(request.getModel());

        log.info("Calling AI to stream outline generation");
        var result = aiApiClient.postSse("/api/outline/generate/stream/mock", request);
        result.doOnSubscribe(sub -> log.info("Subscribed to SSE"))
                .doOnNext(chunk -> log.info("Received chunk: {}", chunk))
                .doOnError(err -> log.error("Stream error", err))
                .doOnComplete(() -> log.info("Stream completed"))
                .subscribe();

        return result;
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
}
