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

    @PostMapping(value = "/{id}/comments", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @RequireDocumentPermission(scopes = {"comment"})
    public ResponseEntity<AppResponseDto<PresentationCommentResponseDto>> createComment(@PathVariable String id,
            @Valid @RequestBody PresentationCommentCreateRequest request) {
        log.info("Received request to create comment on presentation {}", id);

        String currentUserId = securityContextUtils.getCurrentUserId();
        PresentationCommentResponseDto response = commentManagement.createComment(id, currentUserId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(AppResponseDto.success(response));
    }

    @GetMapping(value = "/{id}/comments", produces = MediaType.APPLICATION_JSON_VALUE)
    @RequireDocumentPermission(scopes = {"read"})
    public ResponseEntity<AppResponseDto<List<PresentationCommentResponseDto>>> getComments(@PathVariable String id) {
        log.info("Received request to get comments for presentation {}", id);

        String currentUserId = securityContextUtils.getCurrentUserId();
        List<PresentationCommentResponseDto> comments = commentManagement.getCommentsByPresentationId(id,
                currentUserId);

        return ResponseEntity.ok(AppResponseDto.success(comments));
    }

    @PutMapping(value = "/{id}/comments/{commentId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AppResponseDto<PresentationCommentResponseDto>> updateComment(@PathVariable String id,
            @PathVariable String commentId,
            @Valid @RequestBody PresentationCommentUpdateRequest request) {
        log.info("Received request to update comment {} on presentation {}", commentId, id);

        String currentUserId = securityContextUtils.getCurrentUserId();
        PresentationCommentResponseDto response = commentManagement
                .updateComment(id, commentId, currentUserId, request);

        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    @DeleteMapping(value = "/{id}/comments/{commentId}")
    public ResponseEntity<AppResponseDto<Void>> deleteComment(@PathVariable String id, @PathVariable String commentId) {
        log.info("Received request to delete comment {} on presentation {}", commentId, id);

        String currentUserId = securityContextUtils.getCurrentUserId();
        commentManagement.deleteComment(id, commentId, currentUserId);

        return ResponseEntity.noContent().build();
    }
}
