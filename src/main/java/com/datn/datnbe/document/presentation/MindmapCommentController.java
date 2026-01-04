package com.datn.datnbe.document.presentation;

import com.datn.datnbe.document.dto.request.MindmapCommentCreateRequest;
import com.datn.datnbe.document.dto.request.MindmapCommentUpdateRequest;
import com.datn.datnbe.document.dto.response.MindmapCommentResponseDto;
import com.datn.datnbe.document.management.MindmapCommentManagement;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import com.datn.datnbe.sharedkernel.security.annotation.RequireDocumentPermission;
import com.datn.datnbe.sharedkernel.security.utils.SecurityContextUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/mindmaps")
@RequiredArgsConstructor
public class MindmapCommentController {

    private final MindmapCommentManagement commentManagement;
    private final SecurityContextUtils securityContextUtils;

    @PostMapping(value = "/{mindmapId}/comments", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @RequireDocumentPermission(scopes = {"comment"})
    public ResponseEntity<AppResponseDto<MindmapCommentResponseDto>> createComment(@PathVariable String mindmapId,
            @Valid @RequestBody MindmapCommentCreateRequest request) {
        log.info("Received request to create comment on mindmap {}", mindmapId);

        String currentUserId = securityContextUtils.getCurrentUserId();
        MindmapCommentResponseDto response = commentManagement.createComment(mindmapId, currentUserId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(AppResponseDto.success(response));
    }

    @GetMapping(value = "/{mindmapId}/comments", produces = MediaType.APPLICATION_JSON_VALUE)
    @RequireDocumentPermission(scopes = {"read"})
    public ResponseEntity<AppResponseDto<List<MindmapCommentResponseDto>>> getComments(@PathVariable String mindmapId) {
        log.info("Received request to get comments for mindmap {}", mindmapId);

        String currentUserId = securityContextUtils.getCurrentUserId();
        List<MindmapCommentResponseDto> comments = commentManagement.getCommentsByMindmapId(mindmapId, currentUserId);

        return ResponseEntity.ok(AppResponseDto.success(comments));
    }

    @PutMapping(value = "/{mindmapId}/comments/{commentId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AppResponseDto<MindmapCommentResponseDto>> updateComment(@PathVariable String mindmapId,
            @PathVariable String commentId,
            @Valid @RequestBody MindmapCommentUpdateRequest request) {
        log.info("Received request to update comment {} on mindmap {}", commentId, mindmapId);

        String currentUserId = securityContextUtils.getCurrentUserId();
        MindmapCommentResponseDto response = commentManagement
                .updateComment(mindmapId, commentId, currentUserId, request);

        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    @DeleteMapping(value = "/{mindmapId}/comments/{commentId}")
    public ResponseEntity<AppResponseDto<Void>> deleteComment(@PathVariable String mindmapId,
            @PathVariable String commentId) {
        log.info("Received request to delete comment {} on mindmap {}", commentId, mindmapId);

        String currentUserId = securityContextUtils.getCurrentUserId();
        commentManagement.deleteComment(mindmapId, commentId, currentUserId);

        return ResponseEntity.noContent().build();
    }
}
