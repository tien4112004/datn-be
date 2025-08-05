package com.datn.aiservice.service.interfaces;

import com.datn.aiservice.dto.request.OutlinePromptRequest;
import com.datn.aiservice.dto.request.SlidePromptRequest;
import reactor.core.publisher.Flux;

public interface ContentGenerationService {

    Flux<String> generateOutline(OutlinePromptRequest request);
    Flux<String> generateSlides(SlidePromptRequest request);
}
