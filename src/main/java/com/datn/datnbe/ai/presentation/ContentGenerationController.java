package com.datn.datnbe.ai.presentation;

import com.datn.datnbe.ai.api.ContentGenerationApi;
import com.datn.datnbe.ai.dto.request.OutlinePromptRequest;
import com.datn.datnbe.ai.dto.request.PresentationPromptRequest;
import com.datn.datnbe.ai.utils.StreamingResponseUtils;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping("api/")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class ContentGenerationController {
    ContentGenerationApi contentGenerationExternalApi;

    @PostMapping(value = "presentations/outline-generate", produces = MediaType.TEXT_PLAIN_VALUE)
    public Flux<String> generateOutline(@RequestBody OutlinePromptRequest request) {
        log.info("Received outline generation request: {}", request);

        return StreamingResponseUtils
                .streamWordByWordWithSpaces(StreamingResponseUtils.X_DELAY,
                        contentGenerationExternalApi.generateOutline(request)
                                .doOnSubscribe(subscription -> log.info("Starting outline generation stream")))
                .doOnError(error -> {
                    log.error("Error generating outline", error);
                    throw new AppException(ErrorCode.GENERATION_ERROR, "Failed to generate outline");
                })
                .onErrorMap(error -> new AppException(ErrorCode.GENERATION_ERROR,
                        "Failed to generate outline: " + error.getMessage()));
    }

    @PostMapping(value = "presentations/generate", produces = MediaType.TEXT_PLAIN_VALUE)
    public Flux<String> generateSlides(@RequestBody PresentationPromptRequest request) {

        return contentGenerationExternalApi.generateSlides(request)
                .bufferUntil(token -> token.contains("---"))
                .map(bufferedToken -> {
                    String combined = String.join("", bufferedToken);
                    combined = combined.replace("---", "");
                    return combined;
                })
                .doOnNext(response -> log.info("{}", response))
                .doOnError(error -> log.error("Error generating slides", error));
    }
}
