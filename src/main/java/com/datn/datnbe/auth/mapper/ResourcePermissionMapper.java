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
    @Mapping(target = "sharedWithUserId", source = "targetUserId")
    @Mapping(target = "sharedWithUsername", source = "targetUserId")
    @Mapping(target = "grantedPermissions", source = "permissions")
    @Mapping(target = "message", expression = "java(buildShareMessage(permissions))")
    ResourceShareResponse toResourceShareResponse(String documentId, String targetUserId, Set<String> permissions);

    default String buildShareMessage(Set<String> permissions) {
        return "Successfully shared resource with " + String.join(", ", permissions) + " permissions";
    }
}
