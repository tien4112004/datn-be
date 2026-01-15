package com.datn.datnbe.document.management;

import com.datn.datnbe.auth.api.UserProfileApi;
import com.datn.datnbe.auth.dto.response.UserProfileResponse;
import com.datn.datnbe.document.dto.request.PresentationCommentCreateRequest;
import com.datn.datnbe.document.dto.request.PresentationCommentUpdateRequest;
import com.datn.datnbe.document.dto.response.PresentationCommentResponseDto;
import com.datn.datnbe.document.entity.PresentationComment;
import com.datn.datnbe.document.mapper.PresentationCommentMapper;
import com.datn.datnbe.document.repository.PresentationCommentRepository;
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
public class PresentationCommentManagement {

    PresentationCommentRepository commentRepository;
    PresentationCommentMapper commentMapper;
    UserProfileApi userProfileManagement;

    /**
     * Creates a new comment on a presentation
     *
     * @param presentationId ID of the presentation
     * @param userId ID of the user creating the comment
     * @param request comment creation request
     * @return enriched comment response DTO
     */
    @Transactional
    public PresentationCommentResponseDto createComment(String presentationId,
            String userId,
            PresentationCommentCreateRequest request) {
        log.info("Creating comment on presentation {} by user {}", presentationId, userId);

        PresentationComment comment = commentMapper.createRequestToEntity(request);
        comment.setPresentationId(presentationId);
        comment.setUserId(userId);

        PresentationComment savedComment = commentRepository.save(comment);
        log.info("Created comment with ID: {}", savedComment.getId());

        return enrichCommentWithUserData(savedComment, userId);
    }

    /**
     * Gets all non-deleted comments for a presentation
     *
     * @param presentationId ID of the presentation
     * @param currentUserId ID of the current user (for isOwner flag)
     * @return list of enriched comment response DTOs
     */
    public List<PresentationCommentResponseDto> getCommentsByPresentationId(String presentationId,
            String currentUserId) {
        log.info("Fetching comments for presentation {}", presentationId);

        List<PresentationComment> comments = commentRepository
                .findByPresentationIdAndDeletedAtIsNullOrderByCreatedAtDesc(presentationId);

        return comments.stream()
                .map(comment -> enrichCommentWithUserData(comment, currentUserId))
                .collect(Collectors.toList());
    }

    /**
     * Updates a comment (only by owner)
     *
     * @param presentationId ID of the presentation
     * @param commentId ID of the comment
     * @param userId ID of the user attempting the update
     * @param request update request
     * @return enriched updated comment response DTO
     */
    @Transactional
    public PresentationCommentResponseDto updateComment(String presentationId,
            String commentId,
            String userId,
            PresentationCommentUpdateRequest request) {
        log.info("Updating comment {} on presentation {} by user {}", commentId, presentationId, userId);

        PresentationComment comment = commentRepository.findByIdAndDeletedAtIsNull(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Comment not found with ID: " + commentId));

        // Verify ownership
        if (!comment.getUserId().equals(userId)) {
            log.error("User {} attempted to update comment {} owned by {}", userId, commentId, comment.getUserId());
            throw new AppException(ErrorCode.FORBIDDEN, "You can only update your own comments");
        }

        // Verify presentation
        if (!comment.getPresentationId().equals(presentationId)) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Comment does not belong to this presentation");
        }

        commentMapper.updateEntityFromRequest(request, comment);
        PresentationComment updatedComment = commentRepository.save(comment);

        log.info("Updated comment {}", commentId);
        return enrichCommentWithUserData(updatedComment, userId);
    }

    /**
     * Deletes a comment (soft delete, only by owner)
     *
     * @param presentationId ID of the presentation
     * @param commentId ID of the comment
     * @param userId ID of the user attempting the deletion
     */
    @Transactional
    public void deleteComment(String presentationId, String commentId, String userId) {
        log.info("Deleting comment {} on presentation {} by user {}", commentId, presentationId, userId);

        PresentationComment comment = commentRepository.findByIdAndDeletedAtIsNull(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Comment not found with ID: " + commentId));

        // Verify ownership
        if (!comment.getUserId().equals(userId)) {
            log.error("User {} attempted to delete comment {} owned by {}", userId, commentId, comment.getUserId());
            throw new AppException(ErrorCode.FORBIDDEN, "You can only delete your own comments");
        }

        // Verify presentation
        if (!comment.getPresentationId().equals(presentationId)) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Comment does not belong to this presentation");
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
    private PresentationCommentResponseDto enrichCommentWithUserData(PresentationComment comment,
            String currentUserId) {
        PresentationCommentResponseDto dto = commentMapper.entityToResponseDto(comment);

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
