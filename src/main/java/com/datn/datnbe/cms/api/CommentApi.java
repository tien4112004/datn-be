package com.datn.datnbe.cms.api;

import com.datn.datnbe.cms.dto.response.CommentResponseDto;
import com.datn.datnbe.cms.dto.request.CommentCreateRequest;
import java.util.List;

public interface CommentApi {
    CommentResponseDto createComment(String postId, CommentCreateRequest request);

    List<CommentResponseDto> getPostComments(String postId);

    CommentResponseDto getCommentById(String id);

    void deleteComment(String id);
}
