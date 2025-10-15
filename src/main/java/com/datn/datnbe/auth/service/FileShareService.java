package com.datn.datnbe.auth.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.datn.datnbe.auth.dto.keycloak.KeycloakPermissionDto;
import com.datn.datnbe.auth.dto.keycloak.KeycloakResourceDto;
import com.datn.datnbe.auth.dto.keycloak.KeycloakUserPolicyDto;
import com.datn.datnbe.auth.dto.request.FileShareRequest;
import com.datn.datnbe.auth.dto.response.FileShareResponse;
import com.datn.datnbe.auth.entity.FileResource;
import com.datn.datnbe.auth.repository.FileResourceRepository;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileShareService {

    private final FileResourceRepository fileResourceRepository;
    private final KeycloakAuthorizationService keycloakAuthzService;

    // Valid permission scopes
    private static final String SCOPE_READ = "read";
    private static final String SCOPE_WRITE = "write";
    private static final String SCOPE_SHARE = "share";

    @Transactional
    public FileShareResponse shareFile(String fileId, String currentUserId, FileShareRequest shareRequest) {
        log.info("User {} sharing file {} with user {}", currentUserId, fileId, shareRequest.getTargetUserId());

        // 1. Validate file exists and current user is the owner
        FileResource file = fileResourceRepository.findById(fileId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "File not found: " + fileId));

        if (!file.getOwnerId().equals(currentUserId)) {
            log.warn("User {} attempted to share file {} owned by {}", currentUserId, fileId, file.getOwnerId());
            throw new AppException(ErrorCode.AUTH_UNAUTHORIZED, "Only the file owner can share this file");
        }

        if (file.getKeycloakResourceId() == null) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "File not registered in Keycloak authorization system");
        }

        // 2. Validate permissions
        Set<String> requestedPermissions = shareRequest.getPermissions();
        validatePermissions(requestedPermissions);

        // 3. Create or get user policy for the target user
        String userPolicyName = String.format("user-%s-policy", shareRequest.getTargetUserId());
        KeycloakUserPolicyDto userPolicy = getOrCreateUserPolicy(userPolicyName, shareRequest.getTargetUserId());

        // 4. Grant permissions
        Set<String> grantedPermissions = new HashSet<>();
        for (String permission : requestedPermissions) {
            try {
                grantPermissionToUser(file, permission, userPolicy.getId());
                grantedPermissions.add(permission);
                log.info("Successfully granted {} permission on file {} to user {}",
                        permission,
                        fileId,
                        shareRequest.getTargetUserId());
            } catch (Exception e) {
                log.error("Failed to grant {} permission: {}", permission, e.getMessage());
                // Continue with other permissions even if one fails
            }
        }

        if (grantedPermissions.isEmpty()) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to grant any permissions");
        }

        // 5. Build and return response
        return FileShareResponse.builder()
                .fileId(fileId)
                .fileName(file.getName())
                .sharedWithUserId(shareRequest.getTargetUserId())
                .sharedWithUsername(shareRequest.getTargetUserId()) // Use userId since username removed
                .grantedPermissions(grantedPermissions)
                .message(String.format("Successfully shared file with %s permissions",
                        String.join(", ", grantedPermissions)))
                .success(true)
                .build();
    }

    private void validatePermissions(Set<String> permissions) {
        Set<String> validPermissions = Set.of(SCOPE_READ, SCOPE_WRITE);

        for (String permission : permissions) {
            if (!validPermissions.contains(permission)) {
                throw new AppException(ErrorCode.VALIDATION_ERROR,
                        String.format("Invalid permission: %s. Valid permissions are: read, write", permission));
            }
        }
    }

    private KeycloakUserPolicyDto getOrCreateUserPolicy(String policyName, String userId) {
        // Try to get existing policy
        KeycloakUserPolicyDto existingPolicy = keycloakAuthzService.getPolicyByName(policyName);

        if (existingPolicy != null) {
            log.debug("Using existing user policy: {}", policyName);
            return existingPolicy;
        }

        // Create new policy
        log.debug("Creating new user policy: {}", policyName);
        KeycloakUserPolicyDto newPolicy = KeycloakUserPolicyDto.builder()
                .name(policyName)
                .description(String.format("Policy for user %s", userId))
                .type("user")
                .logic("POSITIVE")
                .decisionStrategy("UNANIMOUS")
                .users(Set.of(userId))
                .build();

        return keycloakAuthzService.createUserPolicy(newPolicy);
    }

    private void grantPermissionToUser(FileResource file, String scope, String userPolicyId) {
        // Permission naming convention: file-{fileId}-{scope}-permission
        String permissionName = String.format("file-%s-%s-permission", file.getId(), scope);

        // Get existing permission or create new one
        KeycloakPermissionDto permission = keycloakAuthzService.getPermissionByName(permissionName);

        if (permission == null) {
            // Create new permission
            log.debug("Creating new permission: {}", permissionName);
            permission = KeycloakPermissionDto.builder()
                    .name(permissionName)
                    .description(String.format("%s permission for file %s", scope.toUpperCase(), file.getName()))
                    .type("scope")
                    .logic("POSITIVE")
                    .decisionStrategy("AFFIRMATIVE")
                    .resources(Set.of(file.getKeycloakResourceId()))
                    .scopes(Set.of(scope))
                    .policies(new HashSet<>(Set.of(userPolicyId)))
                    .build();

            keycloakAuthzService.createPermission(permission);
        } else {
            // Update existing permission by adding the user policy
            log.debug("Updating existing permission: {}", permissionName);

            // Add the new policy to the existing policies
            Set<String> policies = new HashSet<>(
                    permission.getPolicies() != null ? permission.getPolicies() : new HashSet<>());
            policies.add(userPolicyId);
            permission.setPolicies(policies);

            keycloakAuthzService.updatePermission(permission.getId(), permission);
        }
    }

    @Transactional
    public FileResource registerFileResource(FileResource file) {
        log.info("Registering file {} in Keycloak Authorization Services", file.getId());

        // Create resource in Keycloak
        KeycloakResourceDto resource = KeycloakResourceDto.builder()
                .name(String.format("file-%s", file.getId()))
                .type("urn:file")
                .displayName(file.getName())
                .owner(file.getOwnerId())
                .ownerManagedAccess(true)
                .uris(Set.of(String.format("/api/files/%s", file.getId())))
                .scopes(Set.of(SCOPE_READ, SCOPE_WRITE, SCOPE_SHARE))
                .build();

        KeycloakResourceDto createdResource = keycloakAuthzService.createResource(resource);

        // Update file with Keycloak resource ID
        file.setKeycloakResourceId(createdResource.getId());
        fileResourceRepository.save(file);

        // Create owner policy
        String ownerPolicyName = String.format("file-%s-owner-policy", file.getId());
        KeycloakUserPolicyDto ownerPolicy = KeycloakUserPolicyDto.builder()
                .name(ownerPolicyName)
                .description(String.format("Owner policy for file %s", file.getName()))
                .type("user")
                .logic("POSITIVE")
                .decisionStrategy("UNANIMOUS")
                .users(Set.of(file.getOwnerId()))
                .build();

        KeycloakUserPolicyDto createdOwnerPolicy = keycloakAuthzService.createUserPolicy(ownerPolicy);

        // Create owner permissions for all scopes
        createOwnerPermissions(file, createdOwnerPolicy.getId());

        log.info("Successfully registered file {} with resource ID {}", file.getId(), createdResource.getId());

        return file;
    }

    private void createOwnerPermissions(FileResource file, String ownerPolicyId) {
        for (String scope : Set.of(SCOPE_READ, SCOPE_WRITE, SCOPE_SHARE)) {
            String permissionName = String.format("file-%s-%s-permission", file.getId(), scope);

            KeycloakPermissionDto permission = KeycloakPermissionDto.builder()
                    .name(permissionName)
                    .description(String.format("%s permission for file %s", scope.toUpperCase(), file.getName()))
                    .type("scope")
                    .logic("POSITIVE")
                    .decisionStrategy("AFFIRMATIVE")
                    .resources(Set.of(file.getKeycloakResourceId()))
                    .scopes(Set.of(scope))
                    .policies(Set.of(ownerPolicyId))
                    .build();

            keycloakAuthzService.createPermission(permission);
        }
    }
}
