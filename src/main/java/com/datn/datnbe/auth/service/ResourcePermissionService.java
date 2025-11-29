package com.datn.datnbe.auth.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.datn.datnbe.auth.entity.UserProfile;
import com.datn.datnbe.auth.repository.UserProfileRepo;
import lombok.Getter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.datn.datnbe.auth.mapper.KeycloakDtoMapper;
import com.datn.datnbe.auth.mapper.ResourcePermissionMapper;
import com.datn.datnbe.auth.repository.DocumentResourceMappingRepository;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourcePermissionService {
    private final DocumentResourceMappingRepository mappingRepository;
    private final KeycloakAuthorizationService keycloakAuthzService;
    private final ResourcePermissionMapper mapper;
    private final KeycloakDtoMapper keycloakMapper;
    private final UserProfileRepo userProfileRepo;
    private final ResourceAccessService resourceAccessService;

    @Transactional
    public DocumentRegistrationResponse registerResource(ResourceRegistrationRequest request, String ownerId) {
        String id = request.getId();
        String name = request.getName();
        String resourceType = request.getResourceType();

        log.info("Registering document {} in Keycloak for owner {} with resource type {}", id, name, resourceType);

        // Check if already registered
        if (mappingRepository.existsByDocumentId(id)) {
            throw new AppException(ErrorCode.RESOURCE_ALREADY_EXISTS, "Document " + id + " is already registered");
        }

        // Create resource in Keycloak
        String resourcePath = "/api/" + resourceType + "/" + id;

        KeycloakResourceDto resourceDto = keycloakMapper.toKeycloakResourceDto(id,
                resourceType,
                name,
                ownerId,
                true,
                Set.of(resourcePath),
                Set.of("read", "comment", "share"));

        String keycloakResourceId = keycloakAuthzService.createResource(resourceDto).getId();
        log.info("Created Keycloak resource {} with ID {} and path {}", name, keycloakResourceId, resourcePath);

        // Create owner policy
        String ownerPolicyName = name + "-owner-policy";
        KeycloakUserPolicyDto ownerPolicy = keycloakMapper
                .toKeycloakUserPolicyDto(ownerPolicyName, "Owner policy for " + name, Set.of(ownerId));

        String ownerPolicyId = keycloakAuthzService.createUserPolicy(ownerPolicy).getId();

        // Create ONE permission for owner with ALL scopes
        String ownerPermissionName = name + "-user-" + ownerId + "-permission";
        KeycloakPermissionDto ownerPermission = keycloakMapper.toKeycloakPermissionDto(ownerPermissionName,
                "Owner permissions for " + name,
                "AFFIRMATIVE",
                Set.of(keycloakResourceId),
                Set.of("read", "comment", "share"),
                Set.of(ownerPolicyId));

        keycloakAuthzService.createPermission(ownerPermission);
        log.info("Created owner permission {} with all scopes for owner {}", ownerPermissionName, ownerId);

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

    public ResourcePermissionResponse checkUserPermissions(String documentId, String userToken, String userId) {
        log.debug("Checking permissions for document {} by user {}", documentId, userId);

        // Delegate to ResourceAccessService for permission checking
        return resourceAccessService.checkUserPermissions(documentId, userToken, userId);
    }

    @Transactional
    public ResourceShareResponse shareDocument(String documentId, ResourceShareRequest request, String currentUserId) {
        log.info("Sharing resource {} with user {} - permissions: {}",
                documentId,
                request.getTargetUserId(),
                request.getPermissions());

        // 1. Look up the Keycloak resource from mapping table
        DocumentResourceMapping mapping = mappingRepository.findByDocumentId(documentId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                        "document " + documentId + " not found in Keycloak registry"));

        // 2. Verify ownership using ResourceAccessService
        if (!resourceAccessService.isResourceOwner(documentId, currentUserId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Only the resource owner can share this resource");
        }

        // 3. Determine permission level from scopes
        Set<String> requestedScopes = request.getPermissions();
        PermissionLevel level = determinePermissionLevel(requestedScopes);

        // Retrieve target user's Keycloak ID
        Optional<UserProfile> userProfile = userProfileRepo.findByIdOrKeycloakUserId(request.getTargetUserId());

        // 4. Remove user from OTHER permission groups to avoid conflicts
        removeUserFromOtherGroups(userProfile.get().getKeycloakUserId(), level, mapping);

        // 5. Get or create the appropriate group based on permission level
        String groupId = getOrCreatePermissionGroup(documentId, level, mapping);

        // 6. Add the user to the appropriate group
        keycloakAuthzService.addUserToGroup(userProfile.get().getKeycloakUserId(), groupId);

        log.info("Successfully shared resource {} with user {} at {} level",
                documentId,
                request.getTargetUserId(),
                level);

        return mapper.toResourceShareResponse(documentId, request.getTargetUserId(), requestedScopes);
    }

    private void removeUserFromOtherGroups(String userId,
            PermissionLevel targetLevel,
            DocumentResourceMapping mapping) {
        // Remove from readers group if upgrading to commenter or removing access
        if (targetLevel != PermissionLevel.READER && mapping.getReadersGroupId() != null) {
            try {
                keycloakAuthzService.removeUserFromGroup(userId, mapping.getReadersGroupId());
                log.info("Removed user {} from readers group", userId);
            } catch (Exception e) {
                log.debug("User {} was not in readers group", userId);
            }
        }

        // Remove from commenters group if downgrading to reader or removing access
        if (targetLevel != PermissionLevel.COMMENTER && mapping.getCommentersGroupId() != null) {
            try {
                keycloakAuthzService.removeUserFromGroup(userId, mapping.getCommentersGroupId());
                log.info("Removed user {} from commenters group", userId);
            } catch (Exception e) {
                log.debug("User {} was not in commenters group", userId);
            }
        }
    }

    private PermissionLevel determinePermissionLevel(Set<String> scopes) {
        // Determine the highest permission level based on scopes
        if (scopes.contains("comment")) {
            return PermissionLevel.COMMENTER; // read + comment
        } else if (scopes.contains("read")) {
            return PermissionLevel.READER; // read only
        }
        throw new AppException(ErrorCode.VALIDATION_ERROR, "Invalid permission scopes: " + scopes);
    }

    private String getOrCreatePermissionGroup(String documentId,
            PermissionLevel level,
            DocumentResourceMapping mapping) {
        String groupId = switch (level) {
            case READER -> mapping.getReadersGroupId();
            case COMMENTER -> mapping.getCommentersGroupId();
        };

        // If group already exists, return it
        if (groupId != null) {
            log.debug("Using existing {} group for resource {}", level, documentId);
            return groupId;
        }

        // Create new group for this permission level
        log.info("Creating new {} group for resource {}", level, documentId);

        String groupName = "resource-" + documentId + "-" + level.getGroupSuffix();
        var group = keycloakAuthzService.createGroup(groupName);
        groupId = group.getId();

        // Create group-based policy
        String policyName = groupName + "-policy";
        KeycloakGroupPolicyDto policyDto = keycloakMapper.toKeycloakGroupPolicyDto(policyName,
                level.getDescription() + " for " + documentId,
                List.of(keycloakMapper.toGroupDefinition(groupId)));

        var policy = keycloakAuthzService.createGroupPolicy(policyDto);

        // Create permission with appropriate scopes
        String permissionName = groupName + "-permission";
        KeycloakPermissionDto permissionDto = keycloakMapper.toKeycloakPermissionDto(permissionName,
                level.getDescription() + " permissions for " + documentId,
                "AFFIRMATIVE",
                Set.of(mapping.getKeycloakResourceId()),
                level.getScopes(),
                Set.of(policy.getId()));

        keycloakAuthzService.createPermission(permissionDto);

        // Update mapping with new group ID
        switch (level) {
            case READER -> mapping.setReadersGroupId(groupId);
            case COMMENTER -> mapping.setCommentersGroupId(groupId);
        }
        mappingRepository.save(mapping);

        log.info("Created {} group {} with policy and permission", level, groupName);
        return groupId;
    }

    /**
     * Permission levels following Google Drive model
     */
    @Getter
    private enum PermissionLevel {
        READER("readers", "Read-only access", Set.of("read")),
        COMMENTER("commenters", "Read and comment access", Set.of("read", "comment"));

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
    public void revokeDocumentAccess(String documentId, String targetUserId, String currentUserId) {
        log.info("Revoking access to resource {} from user {}", documentId, targetUserId);

        // Look up the resource mapping
        DocumentResourceMapping mapping = mappingRepository.findByDocumentId(documentId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Document " + documentId + " not found in Keycloak registry"));

        // retrieve keycloak user id of target user
        Optional<UserProfile> userProfile = userProfileRepo.findByIdOrKeycloakUserId(targetUserId);
        if (userProfile.isEmpty()) {
            throw new AppException(ErrorCode.USER_NOT_FOUND, "User " + targetUserId + " not found");
        }
        String keycloakUserId = userProfile.get().getKeycloakUserId();

        // Verify ownership using ResourceAccessService
        if (!resourceAccessService.isResourceOwner(documentId, currentUserId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Only the resource owner can revoke access");
        }

        // Check if resource has been shared (has any groups)
        if (mapping.getReadersGroupId() == null && mapping.getCommentersGroupId() == null) {
            log.warn("Resource {} has not been shared with anyone", documentId);
            return;
        }

        // Remove user from both groups (readers and commenters) if they exist
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

        if (removed) {
            log.info("Successfully revoked access to resource {} from user {}", documentId, targetUserId);
        } else {
            log.warn("User {} did not have access to resource {}", targetUserId, documentId);
        }
    }
}
