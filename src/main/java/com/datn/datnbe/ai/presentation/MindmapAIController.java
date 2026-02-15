package com.datn.datnbe.ai.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.datn.datnbe.ai.dto.AIModificationResponse;
import com.datn.datnbe.ai.dto.request.ExpandNodeRequest;
import com.datn.datnbe.ai.dto.request.RefineBranchRequest;
import com.datn.datnbe.ai.dto.request.RefineNodeContentRequest;
import com.datn.datnbe.ai.service.MindmapAIService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("api/ai/mindmap")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class MindmapAIController {
    MindmapAIService mindmapAIService;

    @PostMapping("/refine-node")
    public ResponseEntity<AIModificationResponse> refineNodeContent(
            @Valid @RequestBody RefineNodeContentRequest request) {
        log.info("Refining mindmap node: {}", request.getNodeId());
        AIModificationResponse response = mindmapAIService.refineNodeContent(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/expand-node")
    public ResponseEntity<AIModificationResponse> expandNodeWithChildren(
            @Valid @RequestBody ExpandNodeRequest request) {
        log.info("Expanding mindmap node with children: {}", request.getNodeId());
        AIModificationResponse response = mindmapAIService.expandNodeWithChildren(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refine-branch")
    public ResponseEntity<AIModificationResponse> refineBranchContent(@Valid @RequestBody RefineBranchRequest request) {
        log.info("Refining mindmap branch with {} nodes", request.getNodes().size());
        AIModificationResponse response = mindmapAIService.refineBranchContent(request);
        return ResponseEntity.ok(response);
    }
}
