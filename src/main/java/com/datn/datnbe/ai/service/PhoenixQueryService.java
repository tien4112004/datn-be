package com.datn.datnbe.ai.service;

import com.datn.datnbe.ai.dto.response.TokenUsageInfoDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.UnknownContentTypeException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Service
public class PhoenixQueryService {

    @Value("${phoenix.api-url:http://localhost:6006}")
    private String phoenixApiUrl;

    @Value("${phoenix.api-key:}")
    private String phoenixApiKey;

    @Value("${phoenix.project-id:}")
    private String projectId;

    private final RestTemplate restTemplate = new RestTemplate();

    public TokenUsageInfoDto getTokenUsageFromPhoenix(String traceId, String operationName) {
        try {
            // Use provided traceId (should be from actual distributed trace in Phoenix)
            if (traceId == null || traceId.isEmpty()) {
                return null;
            }

            // Use GraphQL to get trace by traceId
            String url = phoenixApiUrl + "/graphql";

            // Use string formatting instead of GraphQL variables due to Phoenix API compatibility
            String query = String.format("""
                    query GetTraceCost {
                      project: node(id: "%s") {
                        ... on Project {
                          trace(traceId: "%s") {
                            costSummary {
                              total {
                                cost
                                tokens
                              }
                              prompt {
                                cost
                                tokens
                              }
                              completion {
                                cost
                                tokens
                              }
                            }
                          }
                        }
                      }
                    }
                    """, projectId, traceId);

            Map<String, Object> requestBody = Map.of("query", query);

            log.info("Phoenix GraphQL request - URL: {}, traceId: {}, projectId: {}", url, traceId, projectId);
            log.info("Full request body: {}", requestBody);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            if (phoenixApiKey != null && !phoenixApiKey.isEmpty()) {
                headers.set("Authorization", "Bearer " + phoenixApiKey);
            }

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            try {
                log.info("Querying Phoenix for traceId: {} with projectId: {}", traceId, projectId);
                ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) restTemplate
                        .exchange(url, HttpMethod.POST, requestEntity, Map.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    log.debug("Phoenix response: {}", response.getBody());
                    TokenUsageInfoDto result = parseTokenUsageFromGraphQLResponse(response.getBody());
                    if (result == null) {
                        log.warn("Failed to parse token usage from Phoenix response for traceId: {}. Response: {}",
                                traceId,
                                response.getBody());
                    } else {
                        log.info("Successfully retrieved token usage from Phoenix for traceId: {} - total tokens: {}",
                                traceId,
                                result.getTotalTokens());
                    }
                    return result;
                }

                log.warn("Phoenix returned non-2xx status or empty body for traceId: {}", traceId);
                return null;

            } catch (HttpClientErrorException e) {
                log.error("HTTP error querying Phoenix for traceId: {} - Status: {}, Body: {}",
                        traceId,
                        e.getStatusCode(),
                        e.getResponseBodyAsString());
                return null;
            } catch (UnknownContentTypeException e) {
                log.error("Unknown content type from Phoenix for traceId: {}", traceId, e);
                return null;
            }

        } catch (Exception e) {
            log.error("Unexpected error querying Phoenix for traceId: {}", traceId, e);
            return null;
        }
    }

    private TokenUsageInfoDto parseTokenUsageFromGraphQLResponse(Map<String, Object> graphqlResponse) {
        Map<String, Object> costSummary = getNestedMap(graphqlResponse, "data", "project", "trace", "costSummary");
        if (costSummary == null)
            return null;

        long in = toLong(safe(costSummary, "prompt").get("tokens"));
        long out = toLong(safe(costSummary, "completion").get("tokens"));
        if (in == 0 && out == 0)
            return null;

        TokenUsageInfoDto dto = new TokenUsageInfoDto();
        dto.setInputTokens(in);
        dto.setOutputTokens(out);
        dto.setTotalTokens(in + out);

        Object cost = safe(costSummary, "total").get("cost");
        if (cost instanceof Number) {
            dto.setTotalPrice(BigDecimal.valueOf(((Number) cost).doubleValue()));
        }
        return dto;
    }

    private Map<String, Object> getNestedMap(Map<String, Object> root, String... keys) {
        Object current = root;
        for (String key : keys) {
            if (!(current instanceof Map))
                return null;
            current = ((Map<String, Object>) current).get(key);
        }
        return (current instanceof Map) ? (Map<String, Object>) current : null;
    }

    private Map<String, Object> safe(Map<String, Object> map, String key) {
        return (map != null && map.get(key) instanceof Map) ? (Map<String, Object>) map.get(key) : Map.of();
    }

    private long toLong(Object obj) {
        return obj instanceof Number ? ((Number) obj).longValue() : 0;
    }
}
