package com.datn.datnbe.cms.presentation;

import com.datn.datnbe.cms.api.PostApi;
import com.datn.datnbe.cms.dto.request.PinPostRequest;
import com.datn.datnbe.cms.dto.request.PostCreateRequest;
import com.datn.datnbe.cms.dto.request.PostUpdateRequest;
import com.datn.datnbe.cms.dto.response.PostResponseDto;
import com.datn.datnbe.document.dto.response.AssignmentResponse;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import com.datn.datnbe.sharedkernel.security.annotation.RequireTeacherPermission;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class PostController {

    private final PostApi postApi;

    @GetMapping("/classes/{classId}/posts")
    public ResponseEntity<AppResponseDto<List<PostResponseDto>>> getClassPosts(@PathVariable String classId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String search) {

        PaginatedResponseDto<PostResponseDto> paginatedResponse = postApi
                .getClassPosts(classId, Math.max(0, page - 1), size, type, search);

        return ResponseEntity.ok(
                AppResponseDto.successWithPagination(paginatedResponse.getData(), paginatedResponse.getPagination()));
    }

    @PostMapping("/classes/{classId}/posts")
    @RequireTeacherPermission
    public ResponseEntity<AppResponseDto<PostResponseDto>> createPost(@PathVariable String classId,
            @Valid @RequestBody PostCreateRequest request) {
        PostResponseDto resp = postApi.createPost(classId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(AppResponseDto.success(resp));
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<AppResponseDto<PostResponseDto>> getPost(@PathVariable String postId) {
        return ResponseEntity.ok(AppResponseDto.success(postApi.getPostById(postId)));
    }

    @PutMapping("/posts/{postId}")
    @RequireTeacherPermission
    public ResponseEntity<AppResponseDto<PostResponseDto>> updatePost(@PathVariable String postId,
            @Valid @RequestBody PostUpdateRequest request) {
        return ResponseEntity.ok(AppResponseDto.success(postApi.updatePost(postId, request)));
    }

    @DeleteMapping("/posts/{postId}")
    @RequireTeacherPermission
    public ResponseEntity<AppResponseDto<Void>> deletePost(@PathVariable String postId) {
        postApi.deletePost(postId);
        return ResponseEntity.ok(AppResponseDto.success());
    }

    @PostMapping("/posts/{postId}/pin")
    @RequireTeacherPermission
    public ResponseEntity<AppResponseDto<PostResponseDto>> pinPost(@PathVariable String postId,
            @Valid @RequestBody PinPostRequest request) {
        return ResponseEntity.ok(AppResponseDto.success(postApi.pinPost(postId, request)));
    }

    @GetMapping("/posts/{postId}/assignment")
    public ResponseEntity<AppResponseDto<AssignmentResponse>> getAssignmentByPostId(@PathVariable String postId) {
        return ResponseEntity.ok(AppResponseDto.success(postApi.getAssignmentByPostId(postId)));
    }
}
