package com.datn.datnbe.ai.apiclient;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;

@Component
public class AIApiClient {

    private final RestTemplate restTemplate;
    private final WebClient webClient;

    @Value("${ai.api.base-url:}")
    private String baseUrl;

    @Value("${ai.api.timeout:30000}")
    private int timeout;

    public AIApiClient(RestTemplate restTemplate, WebClient.Builder webClientBuilder) {
        this.restTemplate = restTemplate;
        this.webClient = webClientBuilder
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }

    public <T> T get(String endpoint, Class<T> responseType) {
        return get(endpoint, responseType, null);
    }

    public <T> T get(String endpoint, Class<T> responseType, HttpHeaders headers) {
        String url = buildUrl(endpoint);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.GET, entity, responseType);

        return response.getBody();
    }

    public <T, R> R post(String endpoint, T requestBody, Class<R> responseType) {
        return post(endpoint, requestBody, responseType, null);
    }

    public <T, R> R post(String endpoint, T requestBody, Class<R> responseType, HttpHeaders headers) {
        String url = buildUrl(endpoint);
        HttpEntity<T> entity = new HttpEntity<>(requestBody, headers);

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

        WebClient.RequestBodySpec spec = webClient.post().uri(url).contentType(MediaType.APPLICATION_JSON);

        WebClient.RequestHeadersSpec<?> req = spec.bodyValue(requestBody);
        if (headers != null)
            headers.forEach(req::header);

        // Nhận trực tiếp phần data của SSE dưới dạng String
        return req.accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(String.class)                // <-- KHÔNG dùng ServerSentEvent ở đây
                .filter(s -> s != null && !s.isBlank())
                .map(String::trim);
    }

}
