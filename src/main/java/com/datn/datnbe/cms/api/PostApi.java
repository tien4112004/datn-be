package com.datn.datnbe.cms.api;

import com.datn.datnbe.cms.dto.request.PostCreateRequest;
import com.datn.datnbe.cms.dto.request.PostUpdateRequest;
import com.datn.datnbe.cms.dto.response.PostResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;

public interface PostApi {
    PostResponseDto createPost(String classId, PostCreateRequest request);

    PaginatedResponseDto<PostResponseDto> getClassPosts(String classId, int page, int size, String type, String search);

    PostResponseDto getPostById(String postId);

    PostResponseDto updatePost(String postId, PostUpdateRequest request);

    void deletePost(String postId);

    PostResponseDto pinPost(String postId);
}
