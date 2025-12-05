package com.datn.datnbe.auth.mapper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.datn.datnbe.auth.dto.response.DocumentRegistrationResponse;
import com.datn.datnbe.auth.dto.response.ResourcePermissionResponse;
import com.datn.datnbe.auth.dto.response.ResourceShareResponse;
import com.datn.datnbe.auth.entity.DocumentResourceMapping;

@Mapper(componentModel = "spring")
public interface ResourcePermissionMapper {

    @Mapping(target = "id", source = "mapping.documentId")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "keycloakResourceId", source = "mapping.keycloakResourceId")
    @Mapping(target = "ownerId", source = "ownerId")
    @Mapping(target = "message", constant = "Successfully registered resource in Keycloak")
    DocumentRegistrationResponse toDocumentRegistrationResponse(DocumentResourceMapping mapping,
            String name,
            String ownerId);

    @Mapping(target = "resourceId", source = "documentId")
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "permissions", source = "permissions")
    @Mapping(target = "hasAccess", expression = "java(!permissions.isEmpty())")
    ResourcePermissionResponse toResourcePermissionResponse(String documentId, String userId, Set<String> permissions);

    default ResourcePermissionResponse toResourcePermissionResponse(String documentId,
            String userId,
            List<String> permissions) {
        return toResourcePermissionResponse(documentId, userId, new HashSet<>(permissions));
    }

    @Mapping(target = "resourceId", source = "documentId")
    @Mapping(target = "sharedWithUserIds", source = "targetUserIds")
    @Mapping(target = "grantedPermission", source = "permission")
    @Mapping(target = "successCount", source = "successCount")
    @Mapping(target = "failedCount", source = "failedCount")
    @Mapping(target = "message", expression = "java(buildShareMessage(successCount, failedCount, permission))")
    ResourceShareResponse toResourceShareResponse(String documentId,
            List<String> targetUserIds,
            String permission,
            int successCount,
            int failedCount);

    default String buildShareMessage(int successCount, int failedCount, String permission) {
        if (failedCount == 0) {
            return String
                    .format("Successfully shared resource with %s permission to %d user(s)", permission, successCount);
        } else {
            return String.format("Shared resource with %s permission: %d succeeded, %d failed",
                    permission,
                    successCount,
                    failedCount);
        }
    }
}
