package com.datn.datnbe.document.presentation;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.datn.datnbe.document.api.MindmapApi;
import com.datn.datnbe.document.dto.response.MindmapDto;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Public controller for accessing mindmaps without authentication.
 * This endpoint bypasses permission checks and allows anyone with the ID
 * to view the mindmap.
 *
 * WARNING: This exposes mindmaps publicly. Future enhancement should add
 * validation to check if mindmap is marked as public/shareable.
 *
 * Use case: Flutter WebView embedding for mobile app integration.
 */
@RestController
@RequestMapping("/api/public/mindmaps")
@RequiredArgsConstructor
@Slf4j
public class PublicMindmapController {

    private final MindmapApi mindmapApi;

    /**
     * Get a mindmap by ID without authentication.
     *
     * Note: Does NOT use @RequireDocumentPermission annotation,
     * allowing unauthenticated access.
     *
     * TODO: Add validation to check if mindmap has isPublic flag set to true
     * TODO: Consider implementing share token system for better security
     *
     * @param id The mindmap ID
     * @return The mindmap data
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AppResponseDto<MindmapDto>> getPublicMindmap(@PathVariable String id) {
        log.info("Received public request to get mindmap with id: {}", id);
        MindmapDto response = mindmapApi.getMindmap(id);
        return ResponseEntity.ok(AppResponseDto.success(response));
    }
}
