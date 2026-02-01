package com.datn.datnbe.ai.service;

import com.datn.datnbe.ai.dto.response.TokenUsageInfoDto;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TokenUsageInfoDto getTokenUsageFromPhoenix(String traceId, String operationName) {
        try {
            // Use provided traceId (should be from actual distributed trace in Phoenix)
            if (traceId == null || traceId.isEmpty()) {
                return null;
            }
            log.info("=== PHOENIX_DEBUG: Querying Phoenix for traceId: {}", traceId);

            // Use GraphQL to get trace by traceId
            String url = phoenixApiUrl + "/graphql";

            String query = """
                query GetTrace($traceId: String!) {
                  trace: getTraceByOtelId(traceId: $traceId) {
                    spans(first: 1000) {
                      edges {
                        node {
                          attributes
                        }
                      }
                    }
                  }
                }
                """;

            Map<String, Object> requestBody = Map.of(
                "query", query,
                "variables", Map.of("traceId", traceId)
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            if (phoenixApiKey != null && !phoenixApiKey.isEmpty()) {
                headers.set("Authorization", "Bearer " + phoenixApiKey);
            }

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            try {
                ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) restTemplate
                        .exchange(url, HttpMethod.POST, requestEntity, Map.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    TokenUsageInfoDto result = parseTokenUsageFromGraphQLResponse(response.getBody());
                    return result;
                }

                return null;

            } catch (HttpClientErrorException e) {
                return null;
            } catch (UnknownContentTypeException e) {
                return null;
            }

        } catch (Exception e) {
            return null;
        }
    }

    private TokenUsageInfoDto parseTokenUsageFromGraphQLResponse(Map<String, Object> graphqlResponse) {
        try {
            
            java.util.List<?> edges = extractEdgesFromResponse(graphqlResponse);
            
            if (edges == null || edges.isEmpty()) {
                return null;
            }

            TokenUsageInfoDto tokenUsage = new TokenUsageInfoDto();
            long totalInputTokens = 0L;
            long totalOutputTokens = 0L;
            String model = null;
            String provider = null;
            BigDecimal totalPrice = null;

            for (int i = 0; i < edges.size(); i++) {
                Object edgeObj = edges.get(i);
                
                if (!(edgeObj instanceof Map)) {
                    continue;
                }

                Map<String, Object> attributes = extractAttributesFromEdge((Map<String, Object>) edgeObj);
                
                if (attributes == null) {
                    continue;
                }

                long promptTokens = getTokenCount(attributes, "prompt");
                long completionTokens = getTokenCount(attributes, "completion");
                
                totalInputTokens += promptTokens;
                totalOutputTokens += completionTokens;

                if (model == null) {
                    model = getString(attributes, "model_name");
                }
                if (provider == null) {
                    provider = getString(attributes, "provider");
                }
                if (totalPrice == null) {
                    totalPrice = getPrice(attributes, "price");
                }
            }
            
            // Return result only if we found token data
            if (totalInputTokens > 0 || totalOutputTokens > 0) {
                tokenUsage.setInputTokens(totalInputTokens);
                tokenUsage.setOutputTokens(totalOutputTokens);
                tokenUsage.setTotalTokens(totalInputTokens + totalOutputTokens);
                tokenUsage.setModel(model);
                tokenUsage.setProvider(provider);
                tokenUsage.setTotalPrice(totalPrice);  
                
                return tokenUsage;
            }

            return null;

        } catch (Exception e) {
            return null;
        }
    }

    private java.util.List<?> extractEdgesFromResponse(Map<String, Object> graphqlResponse) {
        Object data = graphqlResponse.get("data");
        if (!(data instanceof Map)) return null;

        Object trace = ((Map<String, Object>) data).get("trace");
        if (!(trace instanceof Map)) return null;

        Object spans = ((Map<String, Object>) trace).get("spans");
        if (!(spans instanceof Map)) return null;

        Object edges = ((Map<String, Object>) spans).get("edges");
        return (edges instanceof java.util.List) ? (java.util.List<?>) edges : null;
    }

    private Map<String, Object> extractAttributesFromEdge(Map<String, Object> edge) {
        
        Object node = edge.get("node");
        if (!(node instanceof Map)) {
            return null;
        }

        Map<String, Object> nodeMap = (Map<String, Object>) node;
        
        Object attributes = nodeMap.get("attributes");
        
        // If attributes is a String (JSON), parse it to Map
        if (attributes instanceof String) {
            try {
                attributes = objectMapper.readValue((String) attributes, Map.class);
            } catch (Exception e) {
                return null;
            }
        }
        
        if (!(attributes instanceof Map)) {
            return null;
        }
        
        return (Map<String, Object>) attributes;
    }

    private long getTokenCount(Map<String, Object> attributes, String key) {
        // Handle nested structure: attributes["llm"]["token_count"]["prompt|completion"]
        Object llm = attributes.get("llm");
        if (llm instanceof Map) {
            Map<String, Object> llmMap = (Map<String, Object>) llm;
            Object tokenCount = llmMap.get("token_count");
            
            if (tokenCount instanceof Map) {
                Map<String, Object> tokenCountMap = (Map<String, Object>) tokenCount;
                Object value = tokenCountMap.get(key);
                return (value instanceof Number) ? ((Number) value).longValue() : 0L;
            }
        }
        
        // Fallback to flat structure for backward compatibility
        Object value = attributes.get("llm.token_count." + key);
        return (value instanceof Number) ? ((Number) value).longValue() : 0L;
    }

    private String getString(Map<String, Object> attributes, String key) {
        // Handle nested structure: attributes["llm"]["model_name|provider"]
        Object llm = attributes.get("llm");
        if (llm instanceof Map) {
            Map<String, Object> llmMap = (Map<String, Object>) llm;
            Object value = llmMap.get(key);
            if (value != null) {
                return String.valueOf(value);
            }
        }
        
        // Fallback to flat structure for backward compatibility
        Object value = attributes.get("llm." + key);
        return value != null ? String.valueOf(value) : null;
    }

    private BigDecimal getPrice(Map<String, Object> attributes, String key) {
        // Handle nested structure: attributes["llm"]["price"]
        Object llm = attributes.get("llm");
        if (llm instanceof Map) {
            Map<String, Object> llmMap = (Map<String, Object>) llm;
            Object value = llmMap.get(key);
            if (value instanceof Number) {
                return BigDecimal.valueOf(((Number) value).doubleValue());
            }
        }
        
        // Fallback to flat structure for backward compatibility
        Object value = attributes.get("llm." + key);
        return (value instanceof Number) ? BigDecimal.valueOf(((Number) value).doubleValue()) : null;
    }
}
