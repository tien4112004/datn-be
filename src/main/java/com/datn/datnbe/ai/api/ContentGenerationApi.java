package com.datn.datnbe.ai.api;


import com.datn.datnbe.ai.dto.request.ImageGenerationRequest;
import com.datn.datnbe.ai.dto.request.OutlinePromptRequest;
import com.datn.datnbe.ai.dto.request.PresentationPromptRequest;
import com.datn.datnbe.ai.dto.response.ImageGenerationResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ContentGenerationApi {

    Flux<String> generateOutline(OutlinePromptRequest request);

    Flux<String> generateSlides(PresentationPromptRequest request);
    
    Mono<ImageGenerationResponse> generateImage(ImageGenerationRequest request);
}
