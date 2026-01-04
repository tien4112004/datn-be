package com.datn.datnbe.document.management;

import com.datn.datnbe.auth.management.UserProfileManagement;
import com.datn.datnbe.auth.dto.response.UserProfileResponse;
import com.datn.datnbe.document.dto.request.MindmapCommentCreateRequest;
import com.datn.datnbe.document.dto.request.MindmapCommentUpdateRequest;
import com.datn.datnbe.document.dto.response.MindmapCommentResponseDto;
import com.datn.datnbe.document.entity.MindmapComment;
import com.datn.datnbe.document.mapper.MindmapCommentMapper;
import com.datn.datnbe.document.repository.MindmapCommentRepository;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class MindmapCommentManagement {

    MindmapCommentRepository commentRepository;
    MindmapCommentMapper commentMapper;
    UserProfileManagement userProfileManagement;

    /**
     * Creates a new comment on a mindmap
     *
     * @param mindmapId ID of the mindmap
     * @param userId ID of the user creating the comment
     * @param request comment creation request
     * @return enriched comment response DTO
     */
    @Transactional
    public MindmapCommentResponseDto createComment(String mindmapId,
            String userId,
            MindmapCommentCreateRequest request) {
        log.info("Creating comment on mindmap {} by user {}", mindmapId, userId);

        MindmapComment comment = commentMapper.createRequestToEntity(request);
        comment.setMindmapId(mindmapId);
        comment.setUserId(userId);

        MindmapComment savedComment = commentRepository.save(comment);
        log.info("Created comment with ID: {}", savedComment.getId());

        return enrichCommentWithUserData(savedComment, userId);
    }

    /**
     * Gets all non-deleted comments for a mindmap
     *
     * @param mindmapId ID of the mindmap
     * @param currentUserId ID of the current user (for isOwner flag)
     * @return list of enriched comment response DTOs
     */
    public List<MindmapCommentResponseDto> getCommentsByMindmapId(String mindmapId, String currentUserId) {
        log.info("Fetching comments for mindmap {}", mindmapId);

        List<MindmapComment> comments = commentRepository
                .findByMindmapIdAndDeletedAtIsNullOrderByCreatedAtDesc(mindmapId);

        return comments.stream()
                .map(comment -> enrichCommentWithUserData(comment, currentUserId))
                .collect(Collectors.toList());
    }

    /**
     * Updates a comment (only by owner)
     *
     * @param mindmapId ID of the mindmap
     * @param commentId ID of the comment
     * @param userId ID of the user attempting the update
     * @param request update request
     * @return enriched updated comment response DTO
     */
    @Transactional
    public MindmapCommentResponseDto updateComment(String mindmapId,
            String commentId,
            String userId,
            MindmapCommentUpdateRequest request) {
        log.info("Updating comment {} on mindmap {} by user {}", commentId, mindmapId, userId);

        MindmapComment comment = commentRepository.findByIdAndDeletedAtIsNull(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Comment not found with ID: " + commentId));

        // Verify ownership
        if (!comment.getUserId().equals(userId)) {
            log.error("User {} attempted to update comment {} owned by {}", userId, commentId, comment.getUserId());
            throw new AppException(ErrorCode.FORBIDDEN, "You can only update your own comments");
        }

        // Verify mindmap
        if (!comment.getMindmapId().equals(mindmapId)) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Comment does not belong to this mindmap");
        }

        commentMapper.updateEntityFromRequest(request, comment);
        MindmapComment updatedComment = commentRepository.save(comment);

        log.info("Updated comment {}", commentId);
        return enrichCommentWithUserData(updatedComment, userId);
    }

    /**
     * Deletes a comment (soft delete, only by owner)
     *
     * @param mindmapId ID of the mindmap
     * @param commentId ID of the comment
     * @param userId ID of the user attempting the deletion
     */
    @Transactional
    public void deleteComment(String mindmapId, String commentId, String userId) {
        log.info("Deleting comment {} on mindmap {} by user {}", commentId, mindmapId, userId);

        MindmapComment comment = commentRepository.findByIdAndDeletedAtIsNull(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Comment not found with ID: " + commentId));

        // Verify ownership
        if (!comment.getUserId().equals(userId)) {
            log.error("User {} attempted to delete comment {} owned by {}", userId, commentId, comment.getUserId());
            throw new AppException(ErrorCode.FORBIDDEN, "You can only delete your own comments");
        }

        // Verify mindmap
        if (!comment.getMindmapId().equals(mindmapId)) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Comment does not belong to this mindmap");
        }

        // Soft delete
        comment.setDeletedAt(LocalDateTime.now());
        commentRepository.save(comment);

        log.info("Soft deleted comment {}", commentId);
    }

    /**
     * Enriches a comment DTO with user data from UserProfileManagement
     *
     * @param comment the comment entity
     * @param currentUserId the current user's ID (for isOwner flag)
     * @return enriched comment response DTO
     */
    private MindmapCommentResponseDto enrichCommentWithUserData(MindmapComment comment, String currentUserId) {
        MindmapCommentResponseDto dto = commentMapper.entityToResponseDto(comment);

        try {
            UserProfileResponse userProfile = userProfileManagement.getUserProfile(comment.getUserId());
            dto.setUserName(userProfile.getFirstName() + " " + userProfile.getLastName());
            dto.setUserAvatar(userProfile.getAvatarUrl());
        } catch (Exception e) {
            log.warn("Failed to fetch user profile for user {}: {}", comment.getUserId(), e.getMessage());
            dto.setUserName("Unknown User");
            dto.setUserAvatar(null);
        }

        dto.setIsOwner(comment.getUserId().equals(currentUserId));

        return dto;
    }
}
