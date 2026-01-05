package com.datn.datnbe.cms.service;

import com.datn.datnbe.auth.api.UserProfileApi;
import com.datn.datnbe.auth.dto.response.UserMinimalInfoDto;
import com.datn.datnbe.cms.api.PostApi;
import com.datn.datnbe.cms.dto.request.PostCreateRequest;
import com.datn.datnbe.cms.dto.request.PostUpdateRequest;
import com.datn.datnbe.cms.dto.response.PostResponseDto;
import com.datn.datnbe.cms.entity.Post;
import com.datn.datnbe.cms.mapper.PostMapper;
import com.datn.datnbe.cms.repository.PostRepository;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginationDto;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import com.datn.datnbe.sharedkernel.security.utils.SecurityContextUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService implements PostApi {

    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final SecurityContextUtils securityContextUtils;
    private final UserProfileApi userProfileApi;

    @Override
    @Transactional
    public PostResponseDto createPost(String classId, PostCreateRequest request) {
        Post post = postMapper.toEntity(request);
        post.setClassId(classId);
        post.setAuthorId(securityContextUtils.getCurrentUserId());
        post.setAllowComments(Boolean.TRUE.equals(request.getAllowComments()));
        if (post.getIsPinned() == null)
            post.setIsPinned(Boolean.FALSE);
        if (post.getCommentCount() == null)
            post.setCommentCount(0);
        Post saved = postRepository.save(post);
        PostResponseDto dto = postMapper.toResponseDto(saved);
        populateAuthorInfo(dto, saved.getAuthorId());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDto<PostResponseDto> getClassPosts(String classId,
            int page,
            int size,
            String type,
            String search) {
        PageRequest pr = PageRequest.of(Math.max(0, page), size);
        Page<Post> p = postRepository.findAllWithFilters(classId, type, search, pr);

        // Extract unique author IDs
        List<String> authorIds = p.getContent().stream().map(Post::getAuthorId).distinct().toList();

        // Fetch all authors into map
        Map<String, UserMinimalInfoDto> authorMap = new HashMap<>();
        for (String authorId : authorIds) {
            UserMinimalInfoDto author = userProfileApi.getUserMinimalInfo(authorId);
            if (author != null) {
                authorMap.put(authorId, author);
            }
        }

        // Map and enrich posts
        List<PostResponseDto> posts = p.getContent().stream().map(post -> {
            PostResponseDto dto = postMapper.toResponseDto(post);
            dto.setAuthor(authorMap.get(post.getAuthorId()));
            return dto;
        }).collect(Collectors.toList());

        PaginatedResponseDto<PostResponseDto> resp = new PaginatedResponseDto<>();
        resp.setData(posts);
        resp.setPagination(PaginationDto.builder()
                .currentPage(p.getNumber() + 1)
                .pageSize(p.getSize())
                .totalItems(p.getTotalElements())
                .totalPages(p.getTotalPages())
                .build());
        return resp;
    }

    @Override
    @Transactional(readOnly = true)
    public PostResponseDto getPostById(String postId) {
        Post p = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Post not found"));
        PostResponseDto dto = postMapper.toResponseDto(p);
        populateAuthorInfo(dto, p.getAuthorId());
        return dto;
    }

    @Override
    @Transactional
    public PostResponseDto updatePost(String postId, PostUpdateRequest request) {
        Post exist = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Post not found"));
        postMapper.updateEntity(request, exist);
        Post saved = postRepository.save(exist);
        PostResponseDto dto = postMapper.toResponseDto(saved);
        populateAuthorInfo(dto, saved.getAuthorId());
        return dto;
    }

    @Override
    @Transactional
    public void deletePost(String postId) {
        postRepository.deleteById(postId);
    }

    @Override
    @Transactional
    public PostResponseDto pinPost(String postId) {
        Post exist = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Post not found"));
        exist.setIsPinned(Boolean.TRUE);
        Post saved = postRepository.save(exist);
        PostResponseDto dto = postMapper.toResponseDto(saved);
        populateAuthorInfo(dto, saved.getAuthorId());
        return dto;
    }

    private void populateAuthorInfo(PostResponseDto dto, String authorId) {
        UserMinimalInfoDto author = userProfileApi.getUserMinimalInfo(authorId);
        if (author != null) {
            dto.setAuthor(author);
        }
    }
}
