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

    @PostMapping(value = "/{id}/comments", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @RequireDocumentPermission(scopes = {"comment"})
    public ResponseEntity<AppResponseDto<MindmapCommentResponseDto>> createComment(@PathVariable String id,
            @Valid @RequestBody MindmapCommentCreateRequest request) {
        log.info("Received request to create comment on mindmap {}", id);

        String currentUserId = securityContextUtils.getCurrentUserId();
        MindmapCommentResponseDto response = commentManagement.createComment(id, currentUserId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(AppResponseDto.success(response));
    }

    @GetMapping(value = "/{id}/comments", produces = MediaType.APPLICATION_JSON_VALUE)
    @RequireDocumentPermission(scopes = {"read"})
    public ResponseEntity<AppResponseDto<List<MindmapCommentResponseDto>>> getComments(@PathVariable String id) {
        log.info("Received request to get comments for mindmap {}", id);

        String currentUserId = securityContextUtils.getCurrentUserId();
        List<MindmapCommentResponseDto> comments = commentManagement.getCommentsByMindmapId(id, currentUserId);

        return ResponseEntity.ok(AppResponseDto.success(comments));
    }

    @PutMapping(value = "/{id}/comments/{commentId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AppResponseDto<MindmapCommentResponseDto>> updateComment(@PathVariable String id,
            @PathVariable String commentId,
            @Valid @RequestBody MindmapCommentUpdateRequest request) {
        log.info("Received request to update comment {} on mindmap {}", commentId, id);

        String currentUserId = securityContextUtils.getCurrentUserId();
        MindmapCommentResponseDto response = commentManagement.updateComment(id, commentId, currentUserId, request);

        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    @DeleteMapping(value = "/{id}/comments/{commentId}")
    public ResponseEntity<AppResponseDto<Void>> deleteComment(@PathVariable String id, @PathVariable String commentId) {
        log.info("Received request to delete comment {} on mindmap {}", commentId, id);

        String currentUserId = securityContextUtils.getCurrentUserId();
        commentManagement.deleteComment(id, commentId, currentUserId);

        return ResponseEntity.noContent().build();
    }
}
