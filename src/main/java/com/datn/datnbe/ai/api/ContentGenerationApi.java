package com.datn.datnbe.ai.api;


import com.datn.datnbe.ai.dto.request.OutlinePromptRequest;
import com.datn.datnbe.ai.dto.request.PresentationPromptRequest;
import reactor.core.publisher.Flux;

public interface ContentGenerationApi {

    Flux<String> generateOutline(OutlinePromptRequest request);

    Flux<String> generateSlides(PresentationPromptRequest request);
}
