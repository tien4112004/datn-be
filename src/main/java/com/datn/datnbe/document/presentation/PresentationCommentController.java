package com.datn.datnbe.document.presentation;

import com.datn.datnbe.document.dto.request.PresentationCommentCreateRequest;
import com.datn.datnbe.document.dto.request.PresentationCommentUpdateRequest;
import com.datn.datnbe.document.dto.response.PresentationCommentResponseDto;
import com.datn.datnbe.document.management.PresentationCommentManagement;
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
@RequestMapping("/api/presentations")
@RequiredArgsConstructor
public class PresentationCommentController {

    private final PresentationCommentManagement commentManagement;
    private final SecurityContextUtils securityContextUtils;

    @PostMapping(value = "/{presentationId}/comments", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @RequireDocumentPermission(scopes = {"comment"})
    public ResponseEntity<AppResponseDto<PresentationCommentResponseDto>> createComment(
            @PathVariable String presentationId,
            @Valid @RequestBody PresentationCommentCreateRequest request) {
        log.info("Received request to create comment on presentation {}", presentationId);

        String currentUserId = securityContextUtils.getCurrentUserId();
        PresentationCommentResponseDto response = commentManagement
                .createComment(presentationId, currentUserId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(AppResponseDto.success(response));
    }

    @GetMapping(value = "/{presentationId}/comments", produces = MediaType.APPLICATION_JSON_VALUE)
    @RequireDocumentPermission(scopes = {"read"})
    public ResponseEntity<AppResponseDto<List<PresentationCommentResponseDto>>> getComments(
            @PathVariable String presentationId) {
        log.info("Received request to get comments for presentation {}", presentationId);

        String currentUserId = securityContextUtils.getCurrentUserId();
        List<PresentationCommentResponseDto> comments = commentManagement.getCommentsByPresentationId(presentationId,
                currentUserId);

        return ResponseEntity.ok(AppResponseDto.success(comments));
    }

    @PutMapping(value = "/{presentationId}/comments/{commentId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AppResponseDto<PresentationCommentResponseDto>> updateComment(
            @PathVariable String presentationId,
            @PathVariable String commentId,
            @Valid @RequestBody PresentationCommentUpdateRequest request) {
        log.info("Received request to update comment {} on presentation {}", commentId, presentationId);

        String currentUserId = securityContextUtils.getCurrentUserId();
        PresentationCommentResponseDto response = commentManagement
                .updateComment(presentationId, commentId, currentUserId, request);

        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    @DeleteMapping(value = "/{presentationId}/comments/{commentId}")
    public ResponseEntity<AppResponseDto<Void>> deleteComment(@PathVariable String presentationId,
            @PathVariable String commentId) {
        log.info("Received request to delete comment {} on presentation {}", commentId, presentationId);

        String currentUserId = securityContextUtils.getCurrentUserId();
        commentManagement.deleteComment(presentationId, commentId, currentUserId);

        return ResponseEntity.noContent().build();
    }
}
