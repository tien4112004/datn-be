package com.datn.datnbe.auth.management;

import com.datn.datnbe.auth.api.ResourcePermissionApi;
import com.datn.datnbe.auth.dto.keycloak.KeycloakGroupPolicyDto;
import com.datn.datnbe.auth.dto.keycloak.KeycloakPermissionDto;
import com.datn.datnbe.auth.dto.keycloak.KeycloakResourceDto;
import com.datn.datnbe.auth.dto.keycloak.KeycloakUserPolicyDto;
import com.datn.datnbe.auth.dto.request.ResourceRegistrationRequest;
import com.datn.datnbe.auth.dto.request.ResourceShareRequest;
import com.datn.datnbe.auth.dto.response.DocumentRegistrationResponse;
import com.datn.datnbe.auth.dto.response.ResourcePermissionResponse;
import com.datn.datnbe.auth.dto.response.ResourceShareResponse;
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

        log.info("Registering document {} in Keycloak for owner {} with resource type {}", id, name, resourceType);

        // Check if already registered
        if (mappingRepository.existsByDocumentId(id)) {
            throw new AppException(ErrorCode.RESOURCE_ALREADY_EXISTS, String.format(RESOURCE_ALREADY_EXISTS_MSG, id));
        }

        // Create resource in Keycloak
        String resourcePath = buildResourcePath(resourceType, id);

        KeycloakResourceDto resourceDto = keycloakMapper
                .toKeycloakResourceDto(id, resourceType, name, ownerId, true, Set.of(resourcePath), Set.of(EDIT_SCOPE));

        String keycloakResourceId = keycloakAuthzService.createResource(resourceDto).getId();
        log.info("Created Keycloak resource {} with ID {} and path {}", name, keycloakResourceId, resourcePath);

        // Create owner policy
        String ownerPolicyName = buildOwnerPolicyName(name);
        KeycloakUserPolicyDto ownerPolicy = keycloakMapper.toKeycloakUserPolicyDto(ownerPolicyName,
                String.format(OWNER_POLICY_DESC_TEMPLATE, name),
                Set.of(ownerId));

        String ownerPolicyId = keycloakAuthzService.createUserPolicy(ownerPolicy).getId();

        // Create edit permission for owner (only edit scope)
        String ownerPermissionName = buildOwnerPermissionName(name, ownerId);
        KeycloakPermissionDto ownerPermission = keycloakMapper.toKeycloakPermissionDto(ownerPermissionName,
                String.format(OWNER_PERMISSION_DESC_TEMPLATE, name),
                "AFFIRMATIVE",
                Set.of(keycloakResourceId),
                Set.of(EDIT_SCOPE),
                Set.of(ownerPolicyId));

        keycloakAuthzService.createPermission(ownerPermission);
        log.info("Created owner permission {} with edit scopes for owner {}", ownerPermissionName, ownerId);

        // Save mapping
        DocumentResourceMapping mapping = DocumentResourceMapping.builder()
                .documentId(id)
                .keycloakResourceId(keycloakResourceId)
                .resourceType(resourceType)
                .build();

        DocumentResourceMapping saved = mappingRepository.save(mapping);
        log.info("Saved mapping: id={} → keycloakResourceId={} (type: {})", id, keycloakResourceId, resourceType);

        return mapper.toDocumentRegistrationResponse(saved, name, ownerId);
    }

    @Override
    public ResourcePermissionResponse checkUserPermissions(String documentId, String userToken, String userId) {
        log.debug("Checking permissions for document {} by user", documentId);

        // 1. Look up the Keycloak resource ID from mapping table
        DocumentResourceMapping mapping = mappingRepository.findByDocumentId(documentId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                        String.format(RESOURCE_NOT_FOUND_MSG, documentId)));

        // 2. Check permissions via Keycloak API using resource ID (not name)
        List<String> permissions = keycloakAuthzService.checkUserPermissions(userToken,
                mapping.getKeycloakResourceId());

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
}
