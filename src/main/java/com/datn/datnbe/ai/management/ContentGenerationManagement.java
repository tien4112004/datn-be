package com.datn.datnbe.ai.management;

import com.datn.datnbe.ai.api.ContentGenerationApi;
import com.datn.datnbe.ai.api.ModelSelectionApi;
import com.datn.datnbe.ai.apiclient.AIApiClient;
import com.datn.datnbe.ai.dto.request.MindmapPromptRequest;
import com.datn.datnbe.ai.dto.request.OutlinePromptRequest;
import com.datn.datnbe.ai.dto.request.PresentationPromptRequest;
import com.datn.datnbe.ai.utils.MappingParamsUtils;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

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

    @Value("${ai.api.outline-batch-endpoint}")
    @NonFinal
    String OUTLINE_BATCH_API_ENDPOINT;

    @Value("${ai.api.presentation-endpoint}")
    @NonFinal
    String PRESENTATION_API_ENDPOINT;

    @Value("${ai.api.presentation-batch-endpoint}")
    @NonFinal
    String PRESENTATION_BATCH_API_ENDPOINT;

    @Value("${ai.api.mindmap-endpoint}")
    @NonFinal
    String MINDMAP_API_ENDPOINT;

    @Override
    public Flux<String> generateOutline(OutlinePromptRequest request) {

        if (!modelSelectionApi.isModelEnabled(request.getModel())) {
            log.error("Model {} is not enabled for outline generation", request.getModel());
            return Flux.error(new AppException(ErrorCode.MODEL_NOT_ENABLED));
        }

        log.info("Calling AI to stream outline generation");
        return aiApiClient.postSse(OUTLINE_API_ENDPOINT, MappingParamsUtils.constructParams(request))
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

        return aiApiClient.postSse(PRESENTATION_API_ENDPOINT, MappingParamsUtils.constructParams(request));
    }

    @Override
    public String generateOutlineBatch(OutlinePromptRequest request) {
        log.info("Starting batch outline generation");

        if (!modelSelectionApi.isModelEnabled(request.getModel())) {
            throw new AppException(ErrorCode.MODEL_NOT_ENABLED);
        }

        log.info("Calling AI to generate outline in batch mode");
        try {
            String result = aiApiClient
                    .post(OUTLINE_BATCH_API_ENDPOINT, MappingParamsUtils.constructParams(request), String.class);
            log.info("Batch outline generation completed successfully");
            return result;
        } catch (Exception e) {
            log.error("Error during batch outline generation", e);
            throw new AppException(ErrorCode.AI_WORKER_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    public String generateSlidesBatch(PresentationPromptRequest request) {
        log.info("Starting batch presentation generation for slides");

        if (!modelSelectionApi.isModelEnabled(request.getModel())) {
            log.error("Model {} is not enabled for slide generation", request.getModel());
            throw new AppException(ErrorCode.MODEL_NOT_ENABLED);
        }

        log.info("Calling AI to generate presentation slides in batch mode");
        try {
            String result = aiApiClient
                    .post(PRESENTATION_BATCH_API_ENDPOINT, MappingParamsUtils.constructParams(request), String.class);
            log.info("Batch presentation generation completed successfully");
            return result;
        } catch (Exception e) {
            log.error("Error during batch presentation generation", e);
            throw new AppException(ErrorCode.AI_WORKER_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    public String generateMindmap(MindmapPromptRequest request) {
        log.info("Starting mindmap generation");

        if (!modelSelectionApi.isModelEnabled(request.getModel())) {
            throw new AppException(ErrorCode.MODEL_NOT_ENABLED);
        }

        log.info("Calling AI to generate mindmap");
        try {
            String result = aiApiClient
                    .post(MINDMAP_API_ENDPOINT, MappingParamsUtils.constructParams(request), String.class);
            log.info("Mindmap generation completed successfully");
            return result;
        } catch (Exception e) {
            log.error("Error during mindmap generation", e);
            throw new AppException(ErrorCode.AI_WORKER_SERVER_ERROR, e.getMessage());
        }
    }
}
