package com.datn.datnbe.document.apiclient;

import java.time.Duration;
import java.util.Optional;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.datn.datnbe.document.config.PexelsConfig;
import com.datn.datnbe.document.dto.response.PexelsImageResponse;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Component
@Slf4j
@RequiredArgsConstructor
public class PexelsApiClient {
    private final WebClient webClient;
    private final PexelsConfig pexelsConfig;

    /**
     * Search photos from Pexels API with retry and error handling
     */
    public Mono<PexelsImageResponse> searchPhotos(String query, String orientation, Integer page, Integer perPage) {

        return webClient.get()
                .uri(uriBuilder -> uriBuilder.scheme("https")
                        .host("api.pexels.com")
                        .path("/v1/search")
                        .queryParam("query", query)
                        .queryParam("page", page)
                        .queryParam("per_page", perPage)
                        .queryParamIfPresent("orientation",
                                Optional.ofNullable(orientation).filter(o -> !o.equals("all")))
                        .build())
                .header("Authorization", pexelsConfig.getKey())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    if (response.statusCode().value() == 429) {
                        log.error("Pexels API rate limit exceeded");
                        return Mono.error(new AppException(ErrorCode.EXTERNAL_API_ERROR,
                                "Image search service temporarily unavailable"));
                    }
                    return Mono.error(new AppException(ErrorCode.EXTERNAL_API_ERROR, "Invalid search request"));
                })
                .onStatus(HttpStatusCode::is5xxServerError, response -> {
                    log.error("Pexels API server error");
                    return Mono
                            .error(new AppException(ErrorCode.EXTERNAL_API_ERROR, "Image search service unavailable"));
                })
                .bodyToMono(PexelsImageResponse.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .filter(throwable -> !(throwable instanceof AppException)))
                .timeout(Duration.ofSeconds(30))
                .doOnError(error -> log.error("Error searching Pexels: {}", error.getMessage()));
    }
}
