package com.datn.datnbe.ai.controller;

import com.datn.datnbe.ai.dto.AIModificationResponse;
import com.datn.datnbe.ai.dto.request.ExpandCombinedTextRequest;
import com.datn.datnbe.ai.dto.request.RefineContentRequest;
import com.datn.datnbe.ai.dto.request.RefineElementTextRequest;
import com.datn.datnbe.ai.dto.request.ReplaceElementImageRequest;
import com.datn.datnbe.ai.dto.request.TransformLayoutRequest;
import com.datn.datnbe.ai.service.AIModificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIModificationController {

    private final AIModificationService modificationService;

    @PostMapping("/refine-content")
    public ResponseEntity<AIModificationResponse> refineContent(@RequestBody RefineContentRequest request) {
        return ResponseEntity.ok(modificationService.refineContent(request));
    }

    @PostMapping("/transform-layout")
    public ResponseEntity<AIModificationResponse> transformLayout(@RequestBody TransformLayoutRequest request) {
        return ResponseEntity.ok(modificationService.transformLayout(request));
    }

    @PostMapping("/refine-element-text")
    public ResponseEntity<AIModificationResponse> refineElementText(@RequestBody RefineElementTextRequest request) {
        return ResponseEntity.ok(modificationService.refineElementText(request));
    }

    @PostMapping("/replace-element-image")
    public ResponseEntity<AIModificationResponse> replaceElementImage(@RequestBody ReplaceElementImageRequest request) {
        return ResponseEntity.ok(modificationService.replaceElementImage(request));
    }

    @PostMapping("/refine-combined-text")
    public ResponseEntity<AIModificationResponse> refineCombinedText(@RequestBody ExpandCombinedTextRequest request) {
        return ResponseEntity.ok(modificationService.refineCombinedText(request));
    }
}
