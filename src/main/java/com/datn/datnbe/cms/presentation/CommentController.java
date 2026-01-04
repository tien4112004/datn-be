package com.datn.datnbe.cms.presentation;

import com.datn.datnbe.cms.api.CommentApi;
import com.datn.datnbe.cms.dto.request.CommentCreateRequest;
import com.datn.datnbe.cms.dto.response.CommentResponseDto;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import com.datn.datnbe.sharedkernel.security.annotation.RequireTeacherPermission;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class CommentController {

    private final CommentApi commentApi;

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<AppResponseDto<CommentResponseDto>> createComment(@PathVariable String postId,
            @Valid @RequestBody CommentCreateRequest request) {
        log.debug("POST /api/posts/{}/comments", postId);
        CommentResponseDto resp = commentApi.createComment(postId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(AppResponseDto.success(resp));
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<AppResponseDto<List<CommentResponseDto>>> getComments(@PathVariable String postId) {
        log.debug("GET /api/posts/{}/comments", postId);
        return ResponseEntity.ok(AppResponseDto.success(commentApi.getPostComments(postId)));
    }

    @GetMapping("/comments/{commentId}")
    public ResponseEntity<AppResponseDto<CommentResponseDto>> getComment(@PathVariable String commentId) {
        log.debug("GET /api/comments/{}", commentId);
        return ResponseEntity.ok(AppResponseDto.success(commentApi.getCommentById(commentId)));
    }

    @DeleteMapping("/comments/{commentId}")
    @RequireTeacherPermission
    public ResponseEntity<AppResponseDto<Void>> deleteComment(@PathVariable String commentId) {
        log.debug("DELETE /api/comments/{}", commentId);
        commentApi.deleteComment(commentId);
        return ResponseEntity.ok(AppResponseDto.success());
    }
}
