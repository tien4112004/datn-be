package com.datn.datnbe.cms.service;

import com.datn.datnbe.auth.api.UserProfileApi;
import com.datn.datnbe.auth.dto.response.UserMinimalInfoDto;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService implements CommentApi {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final SecurityContextUtils securityContextUtils;
    private final com.datn.datnbe.cms.repository.PostRepository postRepository;
    private final UserProfileApi userProfileApi;

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

        CommentResponseDto dto = commentMapper.toResponseDto(saved);
        populateUserInfo(dto, saved.getUserId());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponseDto> getPostComments(String postId) {
        List<Comment> comments = commentRepository.findByPostId(postId);

        // Extract unique user IDs
        List<String> userIds = comments.stream().map(Comment::getUserId).distinct().toList();

        // Fetch all users into map
        Map<String, UserMinimalInfoDto> userMap = new HashMap<>();
        for (String userId : userIds) {
            UserMinimalInfoDto user = userProfileApi.getUserMinimalInfo(userId);
            if (user != null) {
                userMap.put(userId, user);
            }
        }

        // Map and enrich comments
        return comments.stream().map(comment -> {
            CommentResponseDto dto = commentMapper.toResponseDto(comment);
            dto.setUser(userMap.get(comment.getUserId()));
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CommentResponseDto getCommentById(String id) {
        Comment c = commentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Comment not found"));
        CommentResponseDto dto = commentMapper.toResponseDto(c);
        populateUserInfo(dto, c.getUserId());
        return dto;
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

    private void populateUserInfo(CommentResponseDto dto, String userId) {
        UserMinimalInfoDto user = userProfileApi.getUserMinimalInfo(userId);
        if (user != null) {
            dto.setUser(user);
        }
    }
}
