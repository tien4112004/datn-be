package com.datn.datnbe.ai.apiclient;

import java.util.Map;

import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import com.datn.datnbe.sharedkernel.security.utils.SecurityContextUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Flux;

@Slf4j
@Component
public class AIApiClient {

    private final RestTemplate restTemplate;
    private final WebClient webClient;
    private final SecurityContextUtils securityContextUtils;

    @Value("${ai.api.base-url}")
    private String baseUrl;

    public AIApiClient(RestTemplate restTemplate, WebClient webClient, SecurityContextUtils securityContextUtils) {
        this.restTemplate = restTemplate;
        this.webClient = webClient;
        this.securityContextUtils = securityContextUtils;
    }

    public <T> T get(String endpoint, Class<T> responseType) {
        return get(endpoint, responseType, null);
    }

    public <T> T get(String endpoint, Class<T> responseType, HttpHeaders headers) {
        String url = buildUrl(endpoint);
        HttpHeaders enrichedHeaders = addUserIdHeader(headers);
        HttpEntity<?> entity = new HttpEntity<>(enrichedHeaders);

        ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.GET, entity, responseType);

        return response.getBody();
    }

    public <T, R> R post(String endpoint, T requestBody, Class<R> responseType) {
        return post(endpoint, requestBody, responseType, null);
    }

    public <T, R> R post(String endpoint, T requestBody, Class<R> responseType, HttpHeaders headers) {
        String url = buildUrl(endpoint);
        HttpHeaders enrichedHeaders = addUserIdHeader(headers);
        HttpEntity<T> entity = new HttpEntity<>(requestBody, enrichedHeaders);

        ResponseEntity<R> response = restTemplate.exchange(url, HttpMethod.POST, entity, responseType);

        return response.getBody();
    }

    private String buildUrl(String endpoint) {
        if (endpoint.startsWith("http")) {
            return endpoint;
        }

        String url = baseUrl;
        if (!url.endsWith("/") && !endpoint.startsWith("/")) {
            url += "/";
        }
        return url + endpoint;
    }

    public <R> Flux<String> postSse(String endpoint, R requestBody) {
        return postSse(endpoint, requestBody, null);
    }

    public <R> Flux<String> postSse(String endpoint, R requestBody, Map<String, String> headers) {
        String url = buildUrl(endpoint);
        log.info("Making SSE request to: {}", url);

        WebClient.RequestBodySpec spec = webClient.post().uri(url).contentType(MediaType.APPLICATION_JSON);

        WebClient.RequestHeadersSpec<?> req = spec.bodyValue(requestBody);

        // Add X-User-ID header
        try {
            String userId = securityContextUtils.getCurrentUserId();
            req.header("X-User-ID", userId);
        } catch (Exception e) {
            log.warn("Could not get current user ID for X-User-ID header: {}", e.getMessage());
        }

        if (headers != null)
            headers.forEach(req::header);

        return req.accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        resp -> resp.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .map(body -> new AppException(ErrorCode.AI_WORKER_UNPROCESSABLE_ENTITY, body)))
                .onStatus(HttpStatusCode::is5xxServerError,
                        resp -> resp.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .map(body -> new AppException(ErrorCode.AI_WORKER_SERVER_ERROR, body)))
                .bodyToFlux(String.class)
                .onErrorMap(WebClientRequestException.class, ex -> {
                    if (ex.getCause() instanceof java.net.ConnectException
                            || ex.getCause() instanceof java.nio.channels.ClosedChannelException) {
                        return new AppException(ErrorCode.AI_WORKER_UNAVAILABLE, "AI worker is unreachable", ex);
                    }
                    return ex;
                })
                .filter(s -> s != null && !s.isBlank())
                .map(String::trim)
                .doOnComplete(() -> log.info("SSE stream completed"))
                .doOnError(err -> log.error("SSE stream error", err));
    }

    /**
     * Add X-User-ID header to existing headers
     */
    private HttpHeaders addUserIdHeader(HttpHeaders existingHeaders) {
        HttpHeaders headers = existingHeaders != null ? new HttpHeaders() : new HttpHeaders();
        if (existingHeaders != null) {
            headers.addAll(existingHeaders);
        }

        try {
            String userId = securityContextUtils.getCurrentUserId();
            headers.set("X-User-ID", userId);
        } catch (Exception e) {
            log.warn("Could not get current user ID for X-User-ID header: {}", e.getMessage());
        }

        return headers;
    }

}
