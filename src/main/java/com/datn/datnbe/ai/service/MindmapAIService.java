package com.datn.datnbe.ai.service;

import com.datn.datnbe.ai.api.TokenUsageApi;
import com.datn.datnbe.ai.apiclient.AIApiClient;
import com.datn.datnbe.ai.dto.AIModificationResponse;
import com.datn.datnbe.ai.dto.request.ExpandNodeRequest;
import com.datn.datnbe.ai.dto.request.RefineBranchRequest;
import com.datn.datnbe.ai.dto.request.RefineNodeContentRequest;
import com.datn.datnbe.ai.dto.response.TokenUsageInfoDto;
import com.datn.datnbe.ai.entity.TokenUsage;
import com.datn.datnbe.sharedkernel.security.utils.SecurityContextUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MindmapAIService {
    private final AIApiClient aiApiClient;
    private final SecurityContextUtils securityContextUtils;
    private final PhoenixQueryService phoenixQueryService;
    private final TokenUsageApi tokenUsageApi;
    private final ObjectMapper objectMapper;

    // These endpoints must match the AI Worker's router
    private static final String AI_MINDMAP_REFINE_NODE_ENDPOINT = "/api/modification/mindmap/refine-node";
    private static final String AI_MINDMAP_EXPAND_NODE_ENDPOINT = "/api/modification/mindmap/expand-node";
    private static final String AI_MINDMAP_REFINE_BRANCH_ENDPOINT = "/api/modification/mindmap/refine-branch";

    public AIModificationResponse refineNodeContent(RefineNodeContentRequest request) {
        log.info("Refining mindmap node: {}", request.getNodeId());
        setDefaultModelAndProvider(request);
        String traceId = UUID.randomUUID().toString();
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Trace-ID", traceId);

        AIModificationResponse response = aiApiClient
                .post(AI_MINDMAP_REFINE_NODE_ENDPOINT, request, AIModificationResponse.class, headers);

        String requestBody = "";
        try {
            requestBody = objectMapper.writeValueAsString(request);
        } catch (Exception e) {
            log.error("Failed to serialize refine node request for token usage tracking", e);
        }
        trackingTokenUsage(traceId, request.getModel(), request.getProvider(), "refine_mindmap_node", requestBody);

        return response;
    }

    public AIModificationResponse expandNodeWithChildren(ExpandNodeRequest request) {
        log.info("Expanding mindmap node with children: {}", request.getNodeId());
        setDefaultModelAndProvider(request);
        String traceId = UUID.randomUUID().toString();
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Trace-ID", traceId);

        AIModificationResponse response = aiApiClient
                .post(AI_MINDMAP_EXPAND_NODE_ENDPOINT, request, AIModificationResponse.class, headers);

        String requestBody = "";
        try {
            requestBody = objectMapper.writeValueAsString(request);
        } catch (Exception e) {
            log.error("Failed to serialize expand node request for token usage tracking", e);
        }
        trackingTokenUsage(traceId, request.getModel(), request.getProvider(), "expand_mindmap_node", requestBody);

        return response;
    }

    public AIModificationResponse refineBranchContent(RefineBranchRequest request) {
        log.info("Refining mindmap branch with {} nodes", request.getNodes().size());
        setDefaultModelAndProvider(request);
        String traceId = UUID.randomUUID().toString();
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Trace-ID", traceId);

        AIModificationResponse response = aiApiClient
                .post(AI_MINDMAP_REFINE_BRANCH_ENDPOINT, request, AIModificationResponse.class, headers);

        String requestBody = "";
        try {
            requestBody = objectMapper.writeValueAsString(request);
        } catch (Exception e) {
            log.error("Failed to serialize refine branch request for token usage tracking", e);
        }
        trackingTokenUsage(traceId, request.getModel(), request.getProvider(), "refine_mindmap_branch", requestBody);

        return response;
    }

    /**
     * Set default model and provider if they are null.
     * This provides backward compatibility for clients that don't yet send these fields.
     */
    private void setDefaultModelAndProvider(Object request) {
        if (request instanceof RefineNodeContentRequest) {
            RefineNodeContentRequest req = (RefineNodeContentRequest) request;
            if (req.getModel() == null) {
                req.setModel("gemini-2.0-flash-exp");
            }
            if (req.getProvider() == null) {
                req.setProvider("google");
            }
        } else if (request instanceof ExpandNodeRequest) {
            ExpandNodeRequest req = (ExpandNodeRequest) request;
            if (req.getModel() == null) {
                req.setModel("gemini-2.0-flash-exp");
            }
            if (req.getProvider() == null) {
                req.setProvider("google");
            }
        } else if (request instanceof RefineBranchRequest) {
            RefineBranchRequest req = (RefineBranchRequest) request;
            if (req.getModel() == null) {
                req.setModel("gemini-2.0-flash-exp");
            }
            if (req.getProvider() == null) {
                req.setProvider("google");
            }
        }
    }

    @Async
    private void trackingTokenUsage(String traceId, String model, String provider, String operation, String request) {
        TokenUsageInfoDto token = phoenixQueryService.getTokenUsageFromPhoenix(traceId, operation);
        if (token != null) {
            TokenUsage tokenUsage = TokenUsage.builder()
                    .model(model)
                    .provider(provider)
                    .request(operation)
                    .inputTokens(token.getInputTokens())
                    .outputTokens(token.getOutputTokens())
                    .tokenCount(token.getTotalTokens())
                    .requestBody(request)
                    .build();
            tokenUsageApi.recordTokenUsage(tokenUsage);
        }
    }
}
