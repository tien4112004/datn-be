package com.datn.datnbe.auth.management;

import com.datn.datnbe.auth.api.ClassGroupApi;
import com.datn.datnbe.auth.dto.keycloak.KeycloakGroupDto;
import com.datn.datnbe.auth.service.KeycloakAuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Management service for class group operations in Keycloak.
 * Handles adding/removing users from class groups for access control.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClassGroupManagement implements ClassGroupApi {

    private final KeycloakAuthorizationService keycloakAuthzService;

    private static final String CLASS_GROUP_PREFIX = "class-";

    @Override
    public void addUserToClassGroup(String classId, String keycloakUserId) {
        log.info("Adding user {} to class group {}", keycloakUserId, classId);

        // Get or create the class group
        String groupName = CLASS_GROUP_PREFIX + classId;
        KeycloakGroupDto classGroup = keycloakAuthzService.createGroup(groupName);

        if (keycloakUserId == null) {
            log.warn("Keycloak user ID is null, skipping group assignment for class {}", classId);
            return;
        }

        // Add user to group
        try {
            keycloakAuthzService.addUserToGroup(keycloakUserId, classGroup.getId());
            log.info("Successfully added user {} to class group {}", keycloakUserId, groupName);
        } catch (Exception e) {
            log.error("Failed to add user {} to class group {}: {}", keycloakUserId, groupName, e.getMessage());
        }
    }

    @Override
    public void removeUserFromClassGroup(String classId, String keycloakUserId) {
        log.info("Removing user {} from class group {}", keycloakUserId, classId);

        // Get the class group
        String groupName = CLASS_GROUP_PREFIX + classId;
        KeycloakGroupDto classGroup = keycloakAuthzService.createGroup(groupName);

        if (keycloakUserId == null) {
            log.warn("Keycloak user ID is null, skipping group removal for class {}", classId);
            return;
        }

        // Remove user from group
        try {
            keycloakAuthzService.removeUserFromGroup(keycloakUserId, classGroup.getId());
            log.info("Successfully removed user {} from class group {}", keycloakUserId, groupName);
        } catch (Exception e) {
            log.error("Failed to remove user {} from class group {}: {}", keycloakUserId, groupName, e.getMessage());
        }
    }

    @Override
    public void syncClassGroupMembers(String classId, List<String> keycloakUserIds) {
        log.info("Syncing class group members for class {}", classId);

        // Get or create the class group
        String groupName = CLASS_GROUP_PREFIX + classId;
        KeycloakGroupDto classGroup = keycloakAuthzService.createGroup(groupName);

        int successCount = 0;
        int failedCount = 0;

        for (String keycloakUserId : keycloakUserIds) {
            if (keycloakUserId == null) {
                log.warn("Skipping null Keycloak user ID during sync for class {}", classId);
                failedCount++;
                continue;
            }

            try {
                keycloakAuthzService.addUserToGroup(keycloakUserId, classGroup.getId());
                successCount++;
            } catch (Exception e) {
                log.warn("Failed to add user {} to class group: {}", keycloakUserId, e.getMessage());
                failedCount++;
            }
        }

        log.info("Class group sync completed for class {}: {} succeeded, {} failed",
                classId,
                successCount,
                failedCount);
    }
}
