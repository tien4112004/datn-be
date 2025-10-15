package com.datn.datnbe.auth.service;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.datn.datnbe.auth.dto.keycloak.KeycloakPermissionDto;
import com.datn.datnbe.auth.dto.keycloak.KeycloakResourceDto;
import com.datn.datnbe.auth.dto.keycloak.KeycloakUserPolicyDto;
import com.datn.datnbe.auth.entity.FileResourceMapping;
import com.datn.datnbe.auth.repository.FileResourceMappingRepository;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilePermissionService {
    private final FileResourceMappingRepository mappingRepository;
    private final KeycloakAuthorizationService keycloakAuthzService;

    @Transactional
    public FileResourceMapping registerFile(String fileId, String fileName, String ownerId, String resourceType) {
        log.info("Registering file {} in Keycloak for owner {} with resource type {}", fileId, ownerId, resourceType);

        // Check if already registered
        if (mappingRepository.existsByFileId(fileId)) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "File " + fileId + " is already registered");
        }

        // Use default resource type if not provided
        if (resourceType == null || resourceType.isBlank()) {
            resourceType = "files";
        }

        // Create resource in Keycloak
        String resourceName = "file-" + fileId;
        String resourcePath = "/api/" + resourceType + "/" + fileId;

        KeycloakResourceDto resourceDto = KeycloakResourceDto.builder()
                .name(resourceName)
                .type("urn:" + resourceType)
                .displayName(fileName)
                .owner(ownerId)
                .ownerManagedAccess(true)
                .uris(Set.of(resourcePath))
                .scopes(Set.of("read", "write", "share"))
                .build();

        String keycloakResourceId = keycloakAuthzService.createResource(resourceDto).getId();
        log.info("Created Keycloak resource {} with ID {} and path {}", resourceName, keycloakResourceId, resourcePath);

        // Create owner policy
        String ownerPolicyName = resourceName + "-owner-policy";
        KeycloakUserPolicyDto ownerPolicy = KeycloakUserPolicyDto.builder()
                .name(ownerPolicyName)
                .description("Owner policy for " + fileName)
                .type("user")
                .logic("POSITIVE")
                .decisionStrategy("UNANIMOUS")
                .users(Set.of(ownerId))
                .build();

        String ownerPolicyId = keycloakAuthzService.createUserPolicy(ownerPolicy).getId();

        // Create ONE permission for owner with ALL scopes
        String ownerPermissionName = resourceName + "-user-" + ownerId + "-permission";
        KeycloakPermissionDto ownerPermission = KeycloakPermissionDto.builder()
                .name(ownerPermissionName)
                .description("Owner permissions for " + fileName)
                .type("scope")
                .logic("POSITIVE")
                .decisionStrategy("AFFIRMATIVE")
                .resources(Set.of(keycloakResourceId))
                .scopes(Set.of("read", "write", "share"))  // All scopes in ONE permission
                .policies(Set.of(ownerPolicyId))
                .build();

        keycloakAuthzService.createPermission(ownerPermission);
        log.info("Created owner permission {} with all scopes for owner {}", ownerPermissionName, ownerId);

        // Save mapping
        FileResourceMapping mapping = FileResourceMapping.builder()
                .fileId(fileId)
                .keycloakResourceId(keycloakResourceId)
                .keycloakResourceName(resourceName)
                .resourceType(resourceType)
                .build();

        FileResourceMapping saved = mappingRepository.save(mapping);
        log.info("Saved mapping: fileId={} → keycloakResourceId={} (type: {})",
                fileId,
                keycloakResourceId,
                resourceType);

        return saved;
    }

    public List<String> checkUserPermissions(String fileId, String userToken) {
        log.debug("Checking permissions for file {} by user", fileId);

        // 1. Look up the Keycloak resource ID from mapping table
        FileResourceMapping mapping = mappingRepository.findByFileId(fileId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                        "File " + fileId + " not found in Keycloak registry"));

        // 2. Check permissions via Keycloak API using resource ID (not name)
        List<String> permissions = keycloakAuthzService.checkUserPermissions(userToken,
                mapping.getKeycloakResourceId());

        log.debug("User has permissions {} on file {}", permissions, fileId);
        return permissions;
    }

    @Transactional
    public void shareFile(String fileId, String targetUserId, Set<String> permissions, String currentUserId) {

        log.info("Sharing file {} with user {} - permissions: {}", fileId, targetUserId, permissions);

        // 1. Look up the Keycloak resource from mapping table
        FileResourceMapping mapping = mappingRepository.findByFileId(fileId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                        "File " + fileId + " not found in Keycloak registry"));

        // 2. Get resource details to check ownership
        KeycloakResourceDto resource = keycloakAuthzService.getResource(mapping.getKeycloakResourceId());

        if (!resource.getOwner().equals(currentUserId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Only the file owner can share this file");
        }

        // 3. Create or get the file's shared group (only created on first share)
        String groupName = "file-" + fileId + "-shared";
        String groupId = mapping.getKeycloakGroupId();

        if (groupId == null) {
            // First time sharing - create the group
            log.info("Creating shared group for file {}", fileId);

            var group = keycloakAuthzService.createGroup(groupName);
            groupId = group.getId();

            // Update mapping with group info
            mapping.setKeycloakGroupId(groupId);
            mapping.setKeycloakGroupName(groupName);
            mappingRepository.save(mapping);

            // Create group-based policy
            String groupPolicyName = groupName + "-policy";
            var groupPolicy = keycloakAuthzService
                    .createGroupPolicy(com.datn.datnbe.auth.dto.keycloak.KeycloakGroupPolicyDto.builder()
                            .name(groupPolicyName)
                            .description("Shared access policy for " + fileId)
                            .type("group")
                            .logic("POSITIVE")
                            .decisionStrategy("UNANIMOUS")
                            .groups(java.util.List.of(
                                    com.datn.datnbe.auth.dto.keycloak.KeycloakGroupPolicyDto.GroupDefinition.builder()
                                            .id(groupId)
                                            .extendChildren(false)
                                            .build()))
                            .build());

            // Create permission linking the group policy to the resource
            String permissionName = groupName + "-permission";
            keycloakAuthzService.createPermission(KeycloakPermissionDto.builder()
                    .name(permissionName)
                    .description("Shared permissions for " + fileId)
                    .type("scope")
                    .logic("POSITIVE")
                    .decisionStrategy("AFFIRMATIVE")
                    .resources(Set.of(mapping.getKeycloakResourceId()))
                    .scopes(permissions)
                    .policies(Set.of(groupPolicy.getId()))
                    .build());

            log.info("Created shared group {} with policy and permission for file {}", groupName, fileId);
        } else {
            // Group already exists - just update the permission with new scopes if needed
            String permissionName = groupName + "-permission";
            KeycloakPermissionDto existingPermission = keycloakAuthzService.getPermissionByName(permissionName);

            if (existingPermission != null) {
                // Update scopes to include new permissions (merge with existing)
                Set<String> existingScopes = existingPermission.getScopes();
                Set<String> mergedScopes = existingScopes != null
                        ? new java.util.HashSet<>(existingScopes)
                        : new java.util.HashSet<>();
                mergedScopes.addAll(permissions);

                existingPermission.setScopes(mergedScopes);
                keycloakAuthzService.updatePermission(existingPermission.getId(), existingPermission);

                log.info("Updated permission {} with scopes {}", permissionName, mergedScopes);
            }
        }

        // 4. Add the user to the shared group
        keycloakAuthzService.addUserToGroup(targetUserId, groupId);

        log.info("Successfully shared file {} with user {} - added to group {}", fileId, targetUserId, groupName);
    }

    @Transactional
    public void unregisterFile(String fileId) {
        log.info("Unregistering file {} from Keycloak", fileId);

        FileResourceMapping mapping = mappingRepository.findByFileId(fileId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                        "File " + fileId + " not found in Keycloak registry"));

        // Delete the shared group if it exists
        if (mapping.getKeycloakGroupId() != null) {
            try {
                keycloakAuthzService.deleteGroup(mapping.getKeycloakGroupId());
                log.info("Deleted shared group {} for file {}", mapping.getKeycloakGroupName(), fileId);
            } catch (Exception e) {
                log.warn("Failed to delete group {}: {}", mapping.getKeycloakGroupName(), e.getMessage());
            }
        }

        // Delete resource from Keycloak (this will cascade delete policies and permissions)
        keycloakAuthzService.deleteResource(mapping.getKeycloakResourceId());

        // Delete mapping
        mappingRepository.deleteByFileId(fileId);

        log.info("Unregistered file {} from Keycloak", fileId);
    }

    public String getKeycloakResourceId(String fileId) {
        return mappingRepository.findByFileId(fileId)
                .map(FileResourceMapping::getKeycloakResourceId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                        "File " + fileId + " not registered in Keycloak"));
    }

    @Transactional
    public void cleanupLegacyPermissions(String fileId, String userId) {
        log.info("Cleaning up legacy permissions for file {}", fileId);

        String keycloakResourceId = getKeycloakResourceId(fileId);
        KeycloakResourceDto resource = keycloakAuthzService.getResource(keycloakResourceId);

        // Verify ownership - owner can be String or object {id: "..."}
        String ownerId = resource.getOwner();
        if (ownerId != null && !ownerId.equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Only the owner can cleanup legacy permissions");
        }

        // Delete legacy per-scope permissions if they exist
        String[] legacyScopes = {"read", "write", "share"};
        for (String scope : legacyScopes) {
            String legacyPermissionName = String.format("file-%s-%s-permission", fileId, scope);
            try {
                keycloakAuthzService.deletePermission(legacyPermissionName);
                log.info("Deleted legacy permission: {}", legacyPermissionName);
            } catch (Exception e) {
                log.warn("Could not delete legacy permission {} (may not exist): {}",
                        legacyPermissionName,
                        e.getMessage());
            }
        }

        log.info("Completed cleanup of legacy permissions for file {}", fileId);
    }

    /**
     * Revokes file access from a user by removing them from the shared group.
     *
     * @param fileId The file ID
     * @param targetUserId The user ID to revoke access from
     * @param currentUserId The current user (must be owner)
     */
    @Transactional
    public void revokeFileAccess(String fileId, String targetUserId, String currentUserId) {
        log.info("Revoking access to file {} from user {}", fileId, targetUserId);

        // Look up the file mapping
        FileResourceMapping mapping = mappingRepository.findByFileId(fileId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                        "File " + fileId + " not found in Keycloak registry"));

        // Verify ownership
        KeycloakResourceDto resource = keycloakAuthzService.getResource(mapping.getKeycloakResourceId());
        if (!resource.getOwner().equals(currentUserId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Only the file owner can revoke access");
        }

        // Check if file has been shared
        if (mapping.getKeycloakGroupId() == null) {
            log.warn("File {} has not been shared with anyone", fileId);
            return;
        }

        // Remove user from the shared group
        keycloakAuthzService.removeUserFromGroup(targetUserId, mapping.getKeycloakGroupId());

        log.info("Successfully revoked access to file {} from user {}", fileId, targetUserId);
    }
}
