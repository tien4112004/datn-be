package com.datn.datnbe.ai.service;

import com.datn.datnbe.ai.apiclient.AIApiClient;
import com.datn.datnbe.ai.dto.AIModificationResponse; // Use the one we created or move it
import com.datn.datnbe.ai.dto.requests.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIModificationService {

    private final AIApiClient aiApiClient;

    // These endpoints must match the AI Worker's router
    private static final String WORKER_REFINE_ENDPOINT = "/api/modification/refine";
    private static final String WORKER_LAYOUT_ENDPOINT = "/api/modification/layout";
    private static final String WORKER_EXPAND_ENDPOINT = "/api/modification/expand";
    private static final String WORKER_REFINE_TEXT_ENDPOINT = "/api/modification/refine-text";
    private static final String WORKER_REPLACE_IMAGE_ENDPOINT = "/api/modification/replace-image";

    public AIModificationResponse refineContent(RefineContentRequest request) {
        log.info("Refining content for slide: {}", request.getContext().getSlideId());
        // Delegate to worker
        return aiApiClient.post(WORKER_REFINE_ENDPOINT, request, AIModificationResponse.class);
    }

    public AIModificationResponse transformLayout(TransformLayoutRequest request) {
        log.info("Transforming layout to: {}", request.getTargetType());
        return aiApiClient.post(WORKER_LAYOUT_ENDPOINT, request, AIModificationResponse.class);
    }

    public AIModificationResponse expandSlide(ExpandSlideRequest request) {
        log.info("Expanding slide into {} slides", request.getCount());
        return aiApiClient.post(WORKER_EXPAND_ENDPOINT, request, AIModificationResponse.class);
    }

    public AIModificationResponse refineElementText(RefineElementTextRequest request) {
        log.info("Refining text for element: {}", request.getElementId());
        return aiApiClient.post(WORKER_REFINE_TEXT_ENDPOINT, request, AIModificationResponse.class);
    }

    public AIModificationResponse replaceElementImage(ReplaceElementImageRequest request) {
        log.info("Replacing image for element: {}", request.getElementId());
        return aiApiClient.post(WORKER_REPLACE_IMAGE_ENDPOINT, request, AIModificationResponse.class);
    }
}
