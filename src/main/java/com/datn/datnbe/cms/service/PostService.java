package com.datn.datnbe.cms.service;

import com.datn.datnbe.auth.api.ResourcePermissionApi;
import com.datn.datnbe.auth.api.UserProfileApi;
import com.datn.datnbe.auth.dto.request.ResourceRegistrationRequest;
import com.datn.datnbe.auth.dto.response.UserMinimalInfoDto;
import com.datn.datnbe.cms.api.PostApi;
import com.datn.datnbe.cms.dto.AttachmentDto;
import com.datn.datnbe.cms.dto.LinkedResourceDto;
import com.datn.datnbe.cms.dto.request.AssignmentRenameRequest;
import com.datn.datnbe.cms.dto.request.PinPostRequest;
import com.datn.datnbe.cms.dto.request.PostCreateRequest;
import com.datn.datnbe.cms.dto.request.PostUpdateRequest;
import com.datn.datnbe.cms.dto.response.PostResponseDto;
import com.datn.datnbe.cms.entity.AssignmentPost;
import com.datn.datnbe.cms.entity.Post;
import com.datn.datnbe.cms.entity.PostLinkedResource;
import com.datn.datnbe.cms.mapper.PostLinkedResourceMapper;
import com.datn.datnbe.cms.mapper.PostMapper;
import com.datn.datnbe.cms.repository.AssignmentPostRepository;
import com.datn.datnbe.cms.repository.PostLinkedResourceRepository;
import com.datn.datnbe.cms.repository.PostRepository;
import com.datn.datnbe.document.entity.Assignment;
import com.datn.datnbe.document.mapper.AssignmentMapper;
import com.datn.datnbe.document.repository.AssignmentRepository;
import com.datn.datnbe.document.dto.response.AssignmentResponse;
import com.datn.datnbe.sharedkernel.notification.dto.SendNotificationToUsersRequest;
import com.datn.datnbe.sharedkernel.notification.enums.NotificationType;
import com.datn.datnbe.sharedkernel.notification.service.NotificationService;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginationDto;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import com.datn.datnbe.sharedkernel.security.utils.SecurityContextUtils;
import com.datn.datnbe.student.api.StudentApi;
import com.nimbusds.openid.connect.sdk.assurance.evidences.attachment.Attachment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    private final PostLinkedResourceRepository postLinkedResourceRepository;
    private final PostMapper postMapper;
    private final PostLinkedResourceMapper postLinkedResourceMapper;
    private final SecurityContextUtils securityContextUtils;
    private final UserProfileApi userProfileApi;
    private final LinkedResourceValidationService linkedResourceValidationService;
    private final LinkedResourcePermissionService linkedResourcePermissionService;
    private final LinkedResourceEnricher linkedResourceEnricher;
    private final NotificationService notificationService;
    private final StudentApi studentApi;
    private final AssignmentPostRepository assignmentPostRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentMapper assignmentMapper;
    private final ResourcePermissionApi resourcePermissionApi;

    @Override
    @Transactional
    public PostResponseDto createPost(String classId, PostCreateRequest request) {
        // Validate linked resources exist
        List<LinkedResourceDto> linkedResources = request.getLinkedResources();
        if (linkedResources != null && !linkedResources.isEmpty()) {
            linkedResourceValidationService.validateLinkedResources(linkedResources);
            linkedResourceValidationService.validateAllPermissionLevels(linkedResources);
        }

        Post post = postMapper.toEntity(request);
        post.setClassId(classId);
        post.setAuthorId(securityContextUtils.getCurrentUserId());
        post.setAllowComments(Boolean.TRUE.equals(request.getAllowComments()));
        List<AttachmentDto> attachments = request.getAttachments() == null ? null :
                request.getAttachments().stream()
                        .map(a -> {
                            if (a.getName() == null || a.getName().isEmpty()) {
                                String fallback = a.getUrl() != null
                                        ? a.getUrl().substring(a.getUrl().lastIndexOf('/') + 1)
                                        : null;
                                a.setName(fallback);
                            }
                            return a;
                        })
                        .toList();
        post.setAttachments(attachments);

        // If assignmentId is provided in request, clone the assignment for this post
        if (request.getAssignmentId() != null && !request.getAssignmentId().isEmpty()) {
            try {
                // Fetch original assignment from assignments table using proper repository
                Assignment originalAssignment = assignmentRepository.findById(request.getAssignmentId()).orElse(null);
                if (originalAssignment != null) {
                    // Clone the assignment (create a new copy in assignment_post table)
                    // All settings come from the request (configured in PostCreator)
                    AssignmentPost clonedAssignment = AssignmentPost.builder()
                            .title(request.getAssignmentTitle() != null && !request.getAssignmentTitle().isEmpty()
                                    ? request.getAssignmentTitle()
                                    : originalAssignment.getTitle())
                            .description(originalAssignment.getDescription())
                            .ownerId(securityContextUtils.getCurrentUserId())
                            .subject(originalAssignment.getSubject())
                            .grade(originalAssignment.getGrade())
                            .questions(originalAssignment.getQuestions())
                            .contexts(originalAssignment.getContexts())
                            // Settings from request (configured when creating homework post)
                            .maxSubmissions(request.getMaxSubmissions())
                            .allowRetake(request.getAllowRetake())
                            .shuffleQuestions(request.getShuffleQuestions())
                            .showCorrectAnswers(request.getShowCorrectAnswers())
                            .passingScore(request.getPassingScore())
                            .availableFrom(request.getAvailableFrom())
                            .availableUntil(request.getAvailableUntil())
                            .topics(originalAssignment.getTopics())
                            .matrixCells(originalAssignment.getMatrixCells())
                            .source(originalAssignment.getId())
                            .autoGrade(request.getAutoGrade())
                            .build();

                    // Save to assignment_post table
                    AssignmentPost savedClone = assignmentPostRepository.save(clonedAssignment);
                    post.setAssignmentId(savedClone.getId());

                    // Register the cloned assignment with resource permissions so teacher can access it
                    try {
                        ResourceRegistrationRequest resourceRequest = ResourceRegistrationRequest.builder()
                                .id(savedClone.getId())
                                .name(savedClone.getTitle())
                                .resourceType("assignment")
                                .build();
                        resourcePermissionApi.registerResource(resourceRequest,
                                securityContextUtils.getCurrentUserId());
                        log.info("Registered cloned assignment {} with resource permissions", savedClone.getId());
                    } catch (Exception e) {
                        log.warn("Failed to register cloned assignment {} with resource permissions",
                                savedClone.getId(),
                                e);
                    }

                    log.info("Cloned assignment {} from assignments table to assignment_post {} for post",
                            request.getAssignmentId(),
                            savedClone.getId());
                } else {
                    // Assignment not found, use the original ID (might be a direct reference)
                    post.setAssignmentId(request.getAssignmentId());
                    log.warn("Assignment {} not found for cloning, using direct reference", request.getAssignmentId());
                }
            } catch (Exception e) {
                log.error("Failed to clone assignment {}, using direct reference", request.getAssignmentId(), e);
                post.setAssignmentId(request.getAssignmentId());
            }
        }

        if (post.getIsPinned() == null)
            post.setIsPinned(Boolean.FALSE);
        if (post.getCommentCount() == null)
            post.setCommentCount(0);
        Post saved = postRepository.save(post);

        // Save linked resources to join table
        if (linkedResources != null && !linkedResources.isEmpty()) {
            List<PostLinkedResource> postLinkedResources = postLinkedResourceMapper.toEntityList(linkedResources);
            for (PostLinkedResource plr : postLinkedResources) {
                plr.setPostId(saved.getId());
            }
            postLinkedResourceRepository.saveAll(postLinkedResources);

            // Grant class permissions for linked resources
            // If post allows comments, upgrade all linked resources to comment scope
            if (Boolean.TRUE.equals(request.getAllowComments())) {
                linkedResources.forEach(r -> r.setPermissionLevel("comment"));
            }
            linkedResourcePermissionService.grantClassPermissions(classId, linkedResources);
        }

        PostResponseDto dto = postMapper.toResponseDto(saved);
        populateAuthorInfo(dto, saved.getAuthorId());

        // Send notification to all students in the class
        notifyStudents(classId, saved, "New Post in Class");

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDto<PostResponseDto> getClassPosts(String classId,
            int page,
            int size,
            String type,
            String search) {
        Sort sort = Sort.by(Sort.Order.desc("isPinned"), Sort.Order.desc("createdAt"));
        PageRequest pr = PageRequest.of(Math.max(0, page), size, sort);
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

        // Batch enrich linked resources with title and thumbnail
        List<LinkedResourceDto> allLinkedResources = posts.stream()
                .filter(post -> post.getLinkedResources() != null)
                .flatMap(post -> post.getLinkedResources().stream())
                .toList();
        linkedResourceEnricher.enrichLinkedResources(allLinkedResources);

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

        // Enrich linked resources with title and thumbnail
        if (dto.getLinkedResources() != null && !dto.getLinkedResources().isEmpty()) {
            linkedResourceEnricher.enrichLinkedResources(dto.getLinkedResources());
        }

        return dto;
    }

    @Override
    @Transactional
    public PostResponseDto updatePost(String postId, PostUpdateRequest request) {
        Post exist = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Post not found"));

        // Validate new linked resources
        List<LinkedResourceDto> newLinkedResources = request.getLinkedResources();
        if (newLinkedResources != null && !newLinkedResources.isEmpty()) {
            linkedResourceValidationService.validateLinkedResources(newLinkedResources);
            linkedResourceValidationService.validateAllPermissionLevels(newLinkedResources);
        }

        // Keep track of old linked resources for permission revocation
        List<PostLinkedResource> oldPostLinkedResources = postLinkedResourceRepository.findByPostId(postId);
        List<LinkedResourceDto> oldLinkedResources = postLinkedResourceMapper.toDtoList(oldPostLinkedResources);
        String classId = exist.getClassId();

        postMapper.updateEntity(request, exist);
        List<AttachmentDto> attachments = request.getAttachments() == null ? null :
        request.getAttachments().stream()
                .map(a -> {
                    if (a.getName() == null || a.getName().isEmpty()) {
                        String fallback = a.getUrl() != null
                                ? a.getUrl().substring(a.getUrl().lastIndexOf('/') + 1)
                                : null;
                        a.setName(fallback);
                    }
                    return a;
                })
                .toList();
        exist.setAttachments(attachments);
        Post saved = postRepository.save(exist);

        // Update linked resources in join table
        if (newLinkedResources != null) {
            // Clear the collection from the entity to let Hibernate manage orphan removal
            exist.getPostLinkedResources().clear();

            // Save new linked resources
            if (!newLinkedResources.isEmpty()) {
                List<PostLinkedResource> postLinkedResources = postLinkedResourceMapper
                        .toEntityList(newLinkedResources);
                for (PostLinkedResource plr : postLinkedResources) {
                    plr.setPostId(saved.getId());
                }
                List<PostLinkedResource> savedResources = postLinkedResourceRepository.saveAll(postLinkedResources);
                // Add new resources back to the collection for consistency
                exist.getPostLinkedResources().addAll(savedResources);

                // Grant permissions for new resources
                linkedResourcePermissionService.grantClassPermissions(classId, newLinkedResources);
            }
        }

        // Revoke permissions for removed resources (if no longer referenced)
        linkedResourcePermissionService.revokeUnlinkedPermissions(classId, oldLinkedResources, newLinkedResources);

        PostResponseDto dto = postMapper.toResponseDto(saved);
        populateAuthorInfo(dto, saved.getAuthorId());
        notifyStudents(saved.getClassId(), saved, "A post has been updated");
        return dto;
    }

    @Override
    @Transactional
    public void deletePost(String postId) {
        Post exist = postRepository.findById(postId).orElse(null);
        if (exist != null) {
            List<PostLinkedResource> postLinkedResources = postLinkedResourceRepository.findByPostId(postId);
            List<LinkedResourceDto> linkedResources = postLinkedResourceMapper.toDtoList(postLinkedResources);
            String classId = exist.getClassId();

            // Delete linked resources from join table first
            postLinkedResourceRepository.deleteByPostId(postId);

            // Delete the post
            postRepository.deleteById(postId);

            // Revoke permissions for resources that are no longer referenced
            if (linkedResources != null && !linkedResources.isEmpty()) {
                linkedResourcePermissionService.revokeUnlinkedPermissions(classId, linkedResources, null);
            }
        } else {
            postRepository.deleteById(postId);
        }
    }

    @Override
    @Transactional
    public PostResponseDto pinPost(String postId, PinPostRequest request) {
        Post exist = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Post not found"));
        exist.setIsPinned(request.getPinned());
        Post saved = postRepository.save(exist);
        PostResponseDto dto = postMapper.toResponseDto(saved);
        populateAuthorInfo(dto, saved.getAuthorId());
        notifyStudents(saved.getClassId(),
                saved,
                exist.getIsPinned() ? "A post has been pinned" : "A post has been unpinned");
        return dto;
    }

    private void populateAuthorInfo(PostResponseDto dto, String authorId) {
        UserMinimalInfoDto author = userProfileApi.getUserMinimalInfo(authorId);
        if (author != null) {
            dto.setAuthor(author);
        }
    }

    private void notifyStudents(String classId, Post post, String title) {
        try {
            log.info("Notifying students in class: {}", classId);
            List<String> userIds = studentApi.getEnrolledStudentKeycloakUserIds(classId);
            log.info("Found {} students in class", userIds.size());

            if (userIds.isEmpty()) {
                log.warn("No students found to notify for class {}", classId);
                return;
            }

            String body = post.getContent() != null && post.getContent().length() > 50
                    ? post.getContent().substring(0, 50) + "..."
                    : post.getContent();

            SendNotificationToUsersRequest notiRequest = SendNotificationToUsersRequest.builder()
                    .userIds(userIds)
                    .title(title)
                    .body(body)
                    .type(NotificationType.POST)
                    .referenceId(classId)
                    .data(Map.of("type", "POST", "referenceId", classId))
                    .build();

            notificationService.sendNotificationToUsers(notiRequest);
        } catch (Exception e) {
            log.error("Failed to send notification for new post", e);
        }
    }

    @Override
    public AssignmentResponse getAssignmentByPostId(String postId) {
        log.info("get assignment of post: {}", postId);
        AssignmentPost assignmentPost = postRepository.getAssignmentByPostId(postId);
        if (assignmentPost == null) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Assignment not found");
        }
        return assignmentMapper.toDto(assignmentPost);
    }

    @Override
    @Transactional
    public AssignmentResponse renameAssignment(String assignmentId, AssignmentRenameRequest request) {
        log.info("Renaming assignment: {}", assignmentId);

        // Get the assignment
        AssignmentPost assignmentPost = assignmentPostRepository.findById(assignmentId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Assignment not found"));

        // Update title and description
        if (request.getTitle() != null) {
            assignmentPost.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            assignmentPost.setDescription(request.getDescription());
        }

        // Save the updated assignment
        AssignmentPost savedAssignment = assignmentPostRepository.save(assignmentPost);
        log.info("Successfully renamed assignment {}", savedAssignment.getId());

        return assignmentMapper.toDto(savedAssignment);
    }

    @Override
    @Transactional
    public void sendDeadlineReminder(String postId) {
        log.info("Sending deadline reminder for post: {}", postId);

        // Get the post
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Post not found"));

        // Check if post is an assignment with a due date
        if (post.getAssignmentId() == null || post.getAssignmentId().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "This post is not an assignment");
        }

        if (post.getDueDate() == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Assignment does not have a due date");
        }

        // Check if deadline has passed
        long currentTime = System.currentTimeMillis();
        long dueTime = post.getDueDate().getTime();
        if (currentTime > dueTime) {
            log.warn("Assignment deadline has already passed for post: {}", postId);
            // Still continue to send reminders to those who haven't submitted
        }

        // Get all students enrolled in the class who haven't submitted
        List<String> studentIds = new java.util.ArrayList<>();
        try {
            studentIds = postRepository.findStudentsWithoutSubmissionByPost(post.getClassId(), postId);
        } catch (Exception e) {
            log.error("Error finding students without submission", e);
            throw new AppException(ErrorCode.INVALID_REQUEST, "Failed to fetch students without submission");
        }

        if (studentIds == null || studentIds.isEmpty()) {
            log.info("All students have submitted the assignment for post: {}", postId);
            return;
        }

        // Map student entity IDs to Keycloak user IDs for the notification system
        List<String> keycloakUserIds = studentIds.stream()
                .map(studentApi::getKeycloakUserIdForStudent)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .toList();

        if (keycloakUserIds.isEmpty()) {
            log.warn("Could not resolve any Keycloak user IDs for students without submission, post: {}", postId);
            return;
        }

        // Send notification to students who haven't submitted
        String assignmentTitle = "Nhắc nhở Hạn chót Bài tập";
        String message = "Bạn chưa nộp bài tập. Hạn chót là "
                + new java.text.SimpleDateFormat("HH:mm dd/MM/yyyy").format(post.getDueDate())
                + ". Vui lòng nộp bài trước hạn chót.";

        SendNotificationToUsersRequest notificationRequest = SendNotificationToUsersRequest.builder()
                .userIds(keycloakUserIds)
                .title(assignmentTitle)
                .body(message)
                .type(NotificationType.ASSIGNMENT_DEADLINE)
                .referenceId(postId)
                .build();

        notificationService.sendNotificationToUsers(notificationRequest);
        log.info("Deadline reminder sent to {} students for post: {}", studentIds.size(), postId);
    }
}
