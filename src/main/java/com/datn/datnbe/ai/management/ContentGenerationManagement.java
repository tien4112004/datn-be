package com.datn.datnbe.ai.management;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.datn.datnbe.ai.api.ContentGenerationApi;
import com.datn.datnbe.ai.api.ModelSelectionApi;
import com.datn.datnbe.ai.apiclient.AIApiClient;
import com.datn.datnbe.ai.dto.request.OutlinePromptRequest;
import com.datn.datnbe.ai.dto.request.PresentationPromptRequest;
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
    ModelSelectionApi modelSelectionApi;
    AIApiClient aiApiClient;

    @Value("${ai.api.outline-endpoint}")
    @NonFinal
    String OUTLINE_API_ENDPOINT;

    @Value("${ai.api.presentation-endpoint}")
    @NonFinal
    String PRESENTATION_API_ENDPOINT;
    // AIEventPublisher aiEventPublisher;

    @Override
    public Flux<String> generateOutline(OutlinePromptRequest request) {

        if (!modelSelectionApi.isModelEnabled(request.getModel())) {
            log.error("Model {} is not enabled for outline generation", request.getModel());
            return Flux.error(new AppException(ErrorCode.MODEL_NOT_ENABLED));
        }

        log.info("Calling AI to stream outline generation");
        return aiApiClient.postSse(OUTLINE_API_ENDPOINT, request)
                .map(chunk -> new String(Base64.getDecoder().decode(chunk), StandardCharsets.UTF_8));
    }

    @Override
    public Flux<String> generateSlides(PresentationPromptRequest request) {
        log.info("Starting streaming presentation generation for slides");

        if (!modelSelectionApi.isModelEnabled(request.getModel())) {
            log.error("Model {} is not enabled for slide generation", request.getModel());
            return Flux.error(new AppException(ErrorCode.MODEL_NOT_ENABLED));
        }

        log.info("Calling AI to stream presentation slides");

        return aiApiClient.postSse(PRESENTATION_API_ENDPOINT, request);
    }
}
