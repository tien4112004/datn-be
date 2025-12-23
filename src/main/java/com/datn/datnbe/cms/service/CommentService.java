package com.datn.datnbe.cms.service;

import com.datn.datnbe.cms.api.CommentApi;
import com.datn.datnbe.cms.dto.request.CommentCreateRequest;
import com.datn.datnbe.cms.dto.response.CommentResponseDto;
import com.datn.datnbe.cms.entity.Comment;
import com.datn.datnbe.cms.mapper.CommentMapper;
import com.datn.datnbe.cms.repository.CommentRepository;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import com.datn.datnbe.sharedkernel.security.utils.SecurityContextUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService implements CommentApi {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final SecurityContextUtils securityContextUtils;
    private final com.datn.datnbe.cms.repository.PostRepository postRepository;

    @Override
    @Transactional
    public CommentResponseDto createComment(String postId, CommentCreateRequest request) {
        // Ensure post exists and comments are allowed
        com.datn.datnbe.cms.entity.Post post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Post not found"));
        if (post.getAllowComments() != null && !post.getAllowComments()) {
            throw new AppException(ErrorCode.FORBIDDEN, "Comments are not allowed on this post");
        }

        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(securityContextUtils.getCurrentUserId());
        comment.setContent(request.getContent());

        Comment saved = commentRepository.save(comment);

        // increment post comment count via repository update for atomicity
        postRepository.updateCommentCount(postId, 1);

        return commentMapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponseDto> getPostComments(String postId) {
        return commentRepository.findByPostId(postId)
                .stream()
                .map(commentMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CommentResponseDto getCommentById(String id) {
        Comment c = commentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Comment not found"));
        return commentMapper.toResponseDto(c);
    }

    @Override
    @Transactional
    public void deleteComment(String id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Comment not found"));
        // delete and decrement post comment count
        commentRepository.deleteById(id);
        postRepository.updateCommentCount(comment.getPostId(), -1);
    }
}
