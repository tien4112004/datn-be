package com.datn.datnbe.auth.management;

import com.datn.datnbe.auth.api.ResourcePermissionApi;
import com.datn.datnbe.auth.dto.keycloak.KeycloakGroupPolicyDto;
import com.datn.datnbe.auth.dto.keycloak.KeycloakPermissionDto;
import com.datn.datnbe.auth.dto.keycloak.KeycloakResourceDto;
import com.datn.datnbe.auth.dto.keycloak.KeycloakUserDto;
import com.datn.datnbe.auth.dto.keycloak.KeycloakUserPolicyDto;
import com.datn.datnbe.auth.dto.request.PublicAccessRequest;
import com.datn.datnbe.auth.dto.request.ResourceRegistrationRequest;
import com.datn.datnbe.auth.dto.request.ResourceShareRequest;
import com.datn.datnbe.auth.dto.response.DocumentRegistrationResponse;
import com.datn.datnbe.auth.dto.response.PublicAccessResponse;
import com.datn.datnbe.auth.dto.response.ResourcePermissionResponse;
import com.datn.datnbe.auth.dto.response.ResourceResponse;
import com.datn.datnbe.auth.dto.response.ResourceShareResponse;
import com.datn.datnbe.auth.dto.response.ShareStateResponse;
import com.datn.datnbe.auth.dto.response.SharedUserResponse;
import com.datn.datnbe.auth.entity.DocumentResourceMapping;
import com.datn.datnbe.auth.entity.UserProfile;
import com.datn.datnbe.auth.mapper.KeycloakDtoMapper;
import com.datn.datnbe.auth.mapper.ResourcePermissionMapper;
import com.datn.datnbe.auth.repository.DocumentResourceMappingRepository;
import com.datn.datnbe.auth.repository.UserProfileRepo;
import com.datn.datnbe.auth.service.KeycloakAuthorizationService;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourcePermissionManagement implements ResourcePermissionApi {
    private final DocumentResourceMappingRepository mappingRepository;
    private final KeycloakAuthorizationService keycloakAuthzService;
    private final ResourcePermissionMapper mapper;
    private final KeycloakDtoMapper keycloakMapper;
    private final UserProfileRepo userProfileRepo;

    // Constants for naming conventions
    private static final String OWNER_POLICY_SUFFIX = "-owner-policy";
    private static final String OWNER_PERMISSION_SUFFIX = "-permission";
    private static final String USER_PREFIX = "-user-";
    private static final String POLICY_SUFFIX = "-policy";
    private static final String RESOURCE_PREFIX = "resource-";
    private static final String API_PATH_PREFIX = "/api/";

    // Description templates
    private static final String OWNER_POLICY_DESC_TEMPLATE = "Owner policy for %s";
    private static final String OWNER_PERMISSION_DESC_TEMPLATE = "Owner permissions for %s";
    private static final String LEVEL_DESC_TEMPLATE = "%s for %s";
    private static final String LEVEL_PERMISSION_DESC_TEMPLATE = "%s permissions for %s";

    // Scopes
    private static final String READ_SCOPE = "read";
    private static final String COMMENT_SCOPE = "comment";
    private static final String EDIT_SCOPE = "edit";

    // Error messages
    private static final String RESOURCE_ALREADY_EXISTS_MSG = "Document %s is already registered";
    private static final String RESOURCE_NOT_FOUND_MSG = "document %s not found in Keycloak registry";
    private static final String DOCUMENT_NOT_FOUND_MSG = "Document %s not found in Keycloak registry";
    private static final String USER_NOT_FOUND_MSG = "User %s not found";
    private static final String INVALID_PERMISSION_MSG = "Invalid permission: %s. Allowed values: read, comment";
    private static final String UNAUTHORIZED_SHARE_MSG = "Only users with edit permission can share this resource";
    private static final String UNAUTHORIZED_REVOKE_MSG = "Only users with edit permission can revoke access";

    // Helper methods for naming
    private String buildOwnerPolicyName(String resourceName) {
        return resourceName + OWNER_POLICY_SUFFIX;
    }

    private String buildOwnerPermissionName(String resourceName, String ownerId) {
        return resourceName + USER_PREFIX + ownerId + OWNER_PERMISSION_SUFFIX;
    }

    private String buildResourcePath(String resourceType, String resourceId) {
        return API_PATH_PREFIX + resourceType + "/" + resourceId;
    }

    private String buildGroupName(String documentId, PermissionLevel level) {
        return RESOURCE_PREFIX + documentId + "-" + level.getGroupSuffix();
    }

    private String buildPolicyName(String groupName) {
        return groupName + POLICY_SUFFIX;
    }

    private String buildPermissionName(String groupName) {
        return groupName + OWNER_PERMISSION_SUFFIX;
    }

    @Transactional
    @Override
    public DocumentRegistrationResponse registerResource(ResourceRegistrationRequest request, String ownerId) {
        String id = request.getId();
        String name = request.getName();
        String resourceType = request.getResourceType();

        log.info("Registering document '{}' (ID: {}) in Keycloak for owner '{}' with resource type '{}'",
                name,
                id,
                ownerId,
                resourceType);

        // Check if already registered
        if (mappingRepository.existsByDocumentId(id)) {
            throw new AppException(ErrorCode.RESOURCE_ALREADY_EXISTS, String.format(RESOURCE_ALREADY_EXISTS_MSG, id));
        }

        // Create resource in Keycloak with all scopes
        String resourcePath = buildResourcePath(resourceType, id);

        KeycloakResourceDto resourceDto = keycloakMapper.toKeycloakResourceDto(id,
                resourceType,
                name,
                ownerId,
                true,
                Set.of(resourcePath),
                Set.of(READ_SCOPE, COMMENT_SCOPE, EDIT_SCOPE));

        String keycloakResourceId = keycloakAuthzService.createResource(resourceDto).getId();
        log.info("Created Keycloak resource {} with ID {} and path {}", name, keycloakResourceId, resourcePath);

        // Create owner policy
        String ownerPolicyName = buildOwnerPolicyName(id);
        KeycloakUserPolicyDto ownerPolicy = keycloakMapper.toKeycloakUserPolicyDto(ownerPolicyName,
                String.format(OWNER_POLICY_DESC_TEMPLATE, name),
                Set.of(ownerId));

        String ownerPolicyId = keycloakAuthzService.createUserPolicy(ownerPolicy).getId();

        // Create permission for owner with all scopes (read, comment, edit) - use document ID
        String ownerPermissionName = buildOwnerPermissionName(id, ownerId);
        KeycloakPermissionDto ownerPermission = keycloakMapper.toKeycloakPermissionDto(ownerPermissionName,
                String.format(OWNER_PERMISSION_DESC_TEMPLATE, name),
                "AFFIRMATIVE",
                Set.of(keycloakResourceId),
                Set.of(READ_SCOPE, COMMENT_SCOPE, EDIT_SCOPE),
                Set.of(ownerPolicyId));

        keycloakAuthzService.createPermission(ownerPermission);
        log.info("Created owner permission {} with all scopes (read, comment, edit) for owner {}",
                ownerPermissionName,
                ownerId);

        // Save mapping with the resource URI and owner ID
        DocumentResourceMapping mapping = DocumentResourceMapping.builder()
                .documentId(id)
                .keycloakResourceId(keycloakResourceId)
                .resourceType(resourceType)
                .resourceUri(resourcePath)
                .ownerId(ownerId)
                .thumbnail(request.getThumbnail())
                .build();

        DocumentResourceMapping saved = mappingRepository.save(mapping);
        log.info("Successfully saved mapping: documentId='{}' → keycloakResourceId='{}' (type: '{}'), ownerId='{}'",
                id,
                keycloakResourceId,
                resourceType,
                ownerId);

        return mapper.toDocumentRegistrationResponse(saved, name, ownerId);
    }

    @Override
    public ResourcePermissionResponse checkUserPermissions(String documentId, String userId) {
        log.info("Checking permissions for document {} by user {}", documentId, userId);

        // Look up the resource mapping to get the exact URI used in Keycloak
        DocumentResourceMapping mapping = mappingRepository.findByDocumentId(documentId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                        String.format(RESOURCE_NOT_FOUND_MSG, documentId)));

        log.info("Found mapping for document {}: ownerId='{}', keycloakResourceId='{}', isPublic='{}'",
                documentId,
                mapping.getOwnerId(),
                mapping.getKeycloakResourceId(),
                mapping.getIsPublic());

        // Check if the user is the owner FIRST - owner ALWAYS gets all permissions regardless of public status
        if (mapping.getOwnerId() != null && mapping.getOwnerId().equals(userId)) {
            log.info("User {} is the owner of document {}, granting all permissions", userId, documentId);
            return mapper
                    .toResourcePermissionResponse(documentId, userId, List.of(READ_SCOPE, COMMENT_SCOPE, EDIT_SCOPE));
        }

        // Check if resource is public - grant public permissions to non-owners
        if (Boolean.TRUE.equals(mapping.getIsPublic())) {
            log.info("Resource {} is public (user {} is not owner), granting public permission: {}",
                    documentId,
                    userId,
                    mapping.getPublicPermission());

            // Grant public permission level
            List<String> publicPermissions;
            if ("comment".equals(mapping.getPublicPermission())) {
                publicPermissions = List.of(READ_SCOPE, COMMENT_SCOPE);  // Commenter gets both read and comment
            } else {
                publicPermissions = List.of(READ_SCOPE);  // Default to read-only
            }

            return mapper.toResourcePermissionResponse(documentId, userId, publicPermissions);
        }

        // For non-owners of non-public resources, check Keycloak permissions
        log.info("User {} is NOT the owner of document {} (owner is '{}'), checking Keycloak permissions",
                userId,
                documentId,
                mapping.getOwnerId());

        List<String> permissions = keycloakAuthzService.checkUserPermissions(userId, mapping.getKeycloakResourceId());

        log.debug("User has permissions {} on document {}", permissions, documentId);

        return mapper.toResourcePermissionResponse(documentId, userId, permissions);
    }

    @Transactional
    @Override
    public ResourceShareResponse shareDocument(String documentId, ResourceShareRequest request, String currentUserId) {
        log.info("Sharing resource {} with {} users - permission: {}",
                documentId,
                request.getTargetUserIds().size(),
                request.getPermission());

        // 1. Look up the Keycloak resource from mapping table
        DocumentResourceMapping mapping = mappingRepository.findByDocumentId(documentId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                        String.format(RESOURCE_NOT_FOUND_MSG, documentId)));

        // 2. Verify user has edit permission (only owner has edit permission)
        KeycloakResourceDto resource = keycloakAuthzService.getResource(mapping.getKeycloakResourceId());
        if (!resource.getOwner().equals(currentUserId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED, UNAUTHORIZED_SHARE_MSG);
        }

        // 3. Determine permission level from the single permission
        ResourcePermissionManagement.PermissionLevel level = determinePermissionLevelFromString(
                request.getPermission());

        // 4. Get or create the appropriate group based on permission level
        String groupId = getOrCreatePermissionGroup(documentId, level, mapping);

        // 5. Share with all target users
        int successCount = 0;
        int failedCount = 0;

        for (String targetUserId : request.getTargetUserIds()) {
            try {
                // Retrieve target user's Keycloak ID
                Optional<UserProfile> userProfile = userProfileRepo.findByIdOrKeycloakUserId(targetUserId);

                if (userProfile.isEmpty()) {
                    log.warn("User {} not found, skipping", targetUserId);
                    failedCount++;
                    continue;
                }

                String keycloakUserId = userProfile.get().getKeycloakUserId();

                // Remove user from OTHER permission groups to avoid conflicts
                removeUserFromOtherGroups(keycloakUserId, level, mapping);

                // Add the user to the appropriate group
                keycloakAuthzService.addUserToGroup(keycloakUserId, groupId);

                log.info("Successfully shared resource {} with user {} at {} level", documentId, targetUserId, level);
                successCount++;

            } catch (Exception e) {
                log.error("Failed to share resource {} with user {}: {}", documentId, targetUserId, e.getMessage());
                failedCount++;
            }
        }

        log.info("Sharing completed for resource {}: {} succeeded, {} failed", documentId, successCount, failedCount);

        return mapper.toResourceShareResponse(documentId,
                request.getTargetUserIds(),
                request.getPermission(),
                successCount,
                failedCount);
    }

    private ResourcePermissionManagement.PermissionLevel determinePermissionLevelFromString(String permission) {
        return switch (permission.toLowerCase()) {
            case "read" -> ResourcePermissionManagement.PermissionLevel.READER;
            case "comment" -> ResourcePermissionManagement.PermissionLevel.COMMENTER;
            default ->
                throw new AppException(ErrorCode.VALIDATION_ERROR, String.format(INVALID_PERMISSION_MSG, permission));
        };
    }

    private void removeUserFromOtherGroups(String userId,
            ResourcePermissionManagement.PermissionLevel targetLevel,
            DocumentResourceMapping mapping) {
        // Remove from readers group if not targeting reader level
        if (targetLevel != ResourcePermissionManagement.PermissionLevel.READER && mapping.getReadersGroupId() != null) {
            try {
                keycloakAuthzService.removeUserFromGroup(userId, mapping.getReadersGroupId());
                log.info("Removed user {} from readers group", userId);
            } catch (Exception e) {
                log.debug("User {} was not in readers group", userId);
            }
        }

        // Remove from commenters group if not targeting commenter level
        if (targetLevel != ResourcePermissionManagement.PermissionLevel.COMMENTER
                && mapping.getCommentersGroupId() != null) {
            try {
                keycloakAuthzService.removeUserFromGroup(userId, mapping.getCommentersGroupId());
                log.info("Removed user {} from commenters group", userId);
            } catch (Exception e) {
                log.debug("User {} was not in commenters group", userId);
            }
        }
    }

    private String getOrCreatePermissionGroup(String documentId,
            ResourcePermissionManagement.PermissionLevel level,
            DocumentResourceMapping mapping) {
        String groupId = switch (level) {
            case READER -> mapping.getReadersGroupId();
            case COMMENTER -> mapping.getCommentersGroupId();
            default -> throw new AppException(ErrorCode.VALIDATION_ERROR, "Edit permission cannot be shared");
        };

        // If group already exists, return it
        if (groupId != null) {
            log.debug("Using existing {} group for resource {}", level, documentId);
            return groupId;
        }

        // Create new group for this permission level
        log.info("Creating new {} group for resource {}", level, documentId);

        String groupName = buildGroupName(documentId, level);
        var group = keycloakAuthzService.createGroup(groupName);
        groupId = group.getId();

        // Create group-based policy
        String policyName = buildPolicyName(groupName);
        KeycloakGroupPolicyDto policyDto = keycloakMapper.toKeycloakGroupPolicyDto(policyName,
                String.format(LEVEL_DESC_TEMPLATE, level.getDescription(), documentId),
                List.of(keycloakMapper.toGroupDefinition(groupId)));

        var policy = keycloakAuthzService.createGroupPolicy(policyDto);

        // Create permission with appropriate scopes
        String permissionName = buildPermissionName(groupName);
        KeycloakPermissionDto permissionDto = keycloakMapper.toKeycloakPermissionDto(permissionName,
                String.format(LEVEL_PERMISSION_DESC_TEMPLATE, level.getDescription(), documentId),
                "AFFIRMATIVE",
                Set.of(mapping.getKeycloakResourceId()),
                level.getScopes(),
                Set.of(policy.getId()));

        keycloakAuthzService.createPermission(permissionDto);

        // Update mapping with new group ID
        switch (level) {
            case READER -> mapping.setReadersGroupId(groupId);
            case COMMENTER -> mapping.setCommentersGroupId(groupId);
            default -> throw new AppException(ErrorCode.VALIDATION_ERROR, "Edit permission cannot be shared");
        }
        mappingRepository.save(mapping);

        log.info("Created {} group {} with policy and permission", level, groupName);
        return groupId;
    }

    /**
     * Permission levels: read, comment (shareable), edit (owner only, not shareable)
     * Edit permission only belongs to owner and allows sharing other permissions
     */
    @Getter
    private enum PermissionLevel {
        READER("readers", "Read-only access", Set.of("read")),
        COMMENTER("commenters", "Read and comment access", Set.of("read", "comment")),
        EDITOR("editors", "Edit access (owner only, not shareable)", Set.of(EDIT_SCOPE));

        private final String groupSuffix;
        private final String description;
        private final Set<String> scopes;

        PermissionLevel(String groupSuffix, String description, Set<String> scopes) {
            this.groupSuffix = groupSuffix;
            this.description = description;
            this.scopes = scopes;
        }
    }

    @Transactional
    @Override
    public void revokeDocumentAccess(String documentId, String targetUserId, String currentUserId) {
        log.info("Revoking access to resource {} from user {}", documentId, targetUserId);

        // Look up the resource mapping
        DocumentResourceMapping mapping = mappingRepository.findByDocumentId(documentId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                        String.format(DOCUMENT_NOT_FOUND_MSG, documentId)));

        // retrieve keycloak user id of target user
        Optional<UserProfile> userProfile = userProfileRepo.findByIdOrKeycloakUserId(targetUserId);
        if (userProfile.isEmpty()) {
            throw new AppException(ErrorCode.USER_NOT_FOUND, String.format(USER_NOT_FOUND_MSG, targetUserId));
        }
        String keycloakUserId = userProfile.get().getKeycloakUserId();

        // Verify user has edit permission (only owner)
        KeycloakResourceDto resource = keycloakAuthzService.getResource(mapping.getKeycloakResourceId());
        if (!resource.getOwner().equals(currentUserId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED, UNAUTHORIZED_REVOKE_MSG);
        }

        // Check if resource has been shared (has any groups)
        if (mapping.getReadersGroupId() == null && mapping.getCommentersGroupId() == null
                && mapping.getEditorsGroupId() == null) {
            log.warn("Resource {} has not been shared with anyone", documentId);
            return;
        }

        // Remove user from all groups (readers, commenters, editors) if they exist
        boolean removed = false;
        if (mapping.getReadersGroupId() != null) {
            try {
                keycloakAuthzService.removeUserFromGroup(keycloakUserId, mapping.getReadersGroupId());
                removed = true;
                log.info("Removed user {} from readers group", targetUserId);
            } catch (Exception e) {
                log.debug("User {} was not in readers group", targetUserId);
            }
        }

        if (mapping.getCommentersGroupId() != null) {
            try {
                keycloakAuthzService.removeUserFromGroup(keycloakUserId, mapping.getCommentersGroupId());
                removed = true;
                log.info("Removed user {} from commenters group", targetUserId);
            } catch (Exception e) {
                log.debug("User {} was not in commenters group", targetUserId);
            }
        }

        if (mapping.getEditorsGroupId() != null) {
            try {
                keycloakAuthzService.removeUserFromGroup(keycloakUserId, mapping.getEditorsGroupId());
                removed = true;
                log.info("Removed user {} from editors group", targetUserId);
            } catch (Exception e) {
                log.debug("User {} was not in editors group", targetUserId);
            }
        }

        if (removed) {
            log.info("Successfully revoked access to resource {} from user {}", documentId, targetUserId);
        } else {
            log.warn("User {} did not have access to resource {}", targetUserId, documentId);
        }
    }

    @Override
    public List<String> getAllResourceByTypeOfOwner(String ownerId, String resourceType) {
        log.info("Getting all resources of type {} for owner {}", resourceType, ownerId);
        return mappingRepository.findResourcesByTypeOfOwner(resourceType, ownerId);
    }

    @Override
    public List<ResourceResponse> getAllResource(String ownerId) {
        log.info("Getting all resources for owner {}", ownerId);
        return mappingRepository.findAllResourcesByOwner(ownerId);
    }

    @Override
    public List<SharedUserResponse> getSharedUsers(String documentId, String currentUserId) {
        log.info("Getting shared users for document {} requested by user {}", documentId, currentUserId);

        // 1. Look up the resource mapping
        DocumentResourceMapping mapping = mappingRepository.findByDocumentId(documentId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                        String.format(DOCUMENT_NOT_FOUND_MSG, documentId)));

        // 2. Verify user has edit permission (only owner can view shared users)
        KeycloakResourceDto resource = keycloakAuthzService.getResource(mapping.getKeycloakResourceId());
        if (!resource.getOwner().equals(currentUserId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Only users with edit permission can view shared users");
        }

        List<SharedUserResponse> sharedUsers = new ArrayList<>();

        // 3. Fetch readers group members if group exists
        if (mapping.getReadersGroupId() != null) {
            List<KeycloakUserDto> readers = keycloakAuthzService.getGroupMembers(mapping.getReadersGroupId());
            log.info("Found {} readers for document {}", readers.size(), documentId);

            for (KeycloakUserDto keycloakUser : readers) {
                Optional<UserProfile> userProfile = userProfileRepo.findByKeycloakUserId(keycloakUser.getId());
                if (userProfile.isPresent()) {
                    UserProfile user = userProfile.get();
                    sharedUsers.add(SharedUserResponse.builder()
                            .userId(user.getId())
                            .email(user.getEmail())
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .avatarUrl(user.getAvatarUrl())
                            .permission("read")
                            .build());
                } else {
                    log.warn("UserProfile not found for Keycloak user ID: {}", keycloakUser.getId());
                }
            }
        }

        // 4. Fetch commenters group members if group exists
        if (mapping.getCommentersGroupId() != null) {
            List<KeycloakUserDto> commenters = keycloakAuthzService.getGroupMembers(mapping.getCommentersGroupId());
            log.info("Found {} commenters for document {}", commenters.size(), documentId);

            for (KeycloakUserDto keycloakUser : commenters) {
                Optional<UserProfile> userProfile = userProfileRepo.findByKeycloakUserId(keycloakUser.getId());
                if (userProfile.isPresent()) {
                    UserProfile user = userProfile.get();
                    sharedUsers.add(SharedUserResponse.builder()
                            .userId(user.getId())
                            .email(user.getEmail())
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .avatarUrl(user.getAvatarUrl())
                            .permission("comment")
                            .build());
                } else {
                    log.warn("UserProfile not found for Keycloak user ID: {}", keycloakUser.getId());
                }
            }
        }

        log.info("Returning {} shared users for document {}", sharedUsers.size(), documentId);
        return sharedUsers;
    }

    @Transactional
    @Override
    public PublicAccessResponse setPublicAccess(String documentId, PublicAccessRequest request, String currentUserId) {
        log.info("Setting public access for document {} - isPublic: {}, permission: {}",
                documentId,
                request.getIsPublic(),
                request.getPublicPermission());

        // 1. Find resource mapping
        DocumentResourceMapping mapping = mappingRepository.findByDocumentId(documentId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                        String.format(RESOURCE_NOT_FOUND_MSG, documentId)));

        // 2. Verify requester is owner
        if (!mapping.getOwnerId().equals(currentUserId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Only resource owner can change public access");
        }

        // 3. Validate permission level if setting to public
        if (Boolean.TRUE.equals(request.getIsPublic()) && request.getPublicPermission() != null) {
            if (!Set.of("read", "comment").contains(request.getPublicPermission().toLowerCase())) {
                throw new AppException(ErrorCode.VALIDATION_ERROR,
                        String.format(INVALID_PERMISSION_MSG, request.getPublicPermission()));
            }
        }

        // 4. Update database
        mapping.setIsPublic(request.getIsPublic());
        mapping.setPublicPermission(Boolean.TRUE.equals(request.getIsPublic()) ? request.getPublicPermission() : null);
        mappingRepository.save(mapping);

        log.info("Updated public access for document {}: isPublic={}, publicPermission={}",
                documentId,
                mapping.getIsPublic(),
                mapping.getPublicPermission());

        // 5. Return response (frontend will construct share link)
        return PublicAccessResponse.builder()
                .documentId(documentId)
                .isPublic(mapping.getIsPublic())
                .publicPermission(mapping.getPublicPermission())
                .build();
    }

    @Override
    public PublicAccessResponse getPublicAccessStatus(String documentId, String currentUserId) {
        log.info("Getting public access status for document {} requested by user {}", documentId, currentUserId);

        DocumentResourceMapping mapping = mappingRepository.findByDocumentId(documentId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                        String.format(RESOURCE_NOT_FOUND_MSG, documentId)));

        // Note: We allow any authenticated user to check public access status
        // This is needed for users accessing a public resource via link

        return PublicAccessResponse.builder()
                .documentId(documentId)
                .isPublic(mapping.getIsPublic() != null ? mapping.getIsPublic() : false)
                .publicPermission(mapping.getPublicPermission())
                .build();
    }

    @Override
    public ShareStateResponse getShareState(String documentId, String currentUserId) {
        log.info("Getting complete share state for document {} requested by user {}", documentId, currentUserId);

        // 1. Get shared users (reuse existing method)
        List<SharedUserResponse> sharedUsers = getSharedUsers(documentId, currentUserId);

        // 2. Get public access status (reuse existing method)
        PublicAccessResponse publicAccess = getPublicAccessStatus(documentId, currentUserId);

        // 3. Get current user's permission
        ResourcePermissionResponse permissionResponse = checkUserPermissions(documentId, currentUserId);
        String currentUserPermission = determineHighestPermission(permissionResponse.getPermissions());

        log.info("Share state for document {}: {} shared users, public={}, current user permission={}",
                documentId,
                sharedUsers.size(),
                publicAccess.getIsPublic(),
                currentUserPermission);

        // 4. Build and return combined response
        return ShareStateResponse.builder()
                .sharedUsers(sharedUsers)
                .publicAccess(publicAccess)
                .currentUserPermission(currentUserPermission)
                .build();
    }

    /**
     * Helper: Determine highest permission level from permissions array
     * @param permissions Collection of permission strings (e.g., ["read", "comment", "edit"])
     * @return The highest permission level ("edit" > "comment" > "read")
     */
    private String determineHighestPermission(Collection<String> permissions) {
        if (permissions.contains("edit")) {
            return "edit";
        }
        if (permissions.contains("comment")) {
            return "comment";
        }
        if (permissions.contains("read")) {
            return "read";
        }
        return "read"; // Default fallback
    }

}
