package com.datn.datnbe.auth.mapper;

import java.util.List;
import java.util.Set;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.datn.datnbe.auth.dto.keycloak.KeycloakGroupDto;
import com.datn.datnbe.auth.dto.keycloak.KeycloakGroupPolicyDto;
import com.datn.datnbe.auth.dto.keycloak.KeycloakPermissionDto;
import com.datn.datnbe.auth.dto.keycloak.KeycloakResourceDto;
import com.datn.datnbe.auth.dto.keycloak.KeycloakUserPolicyDto;

@Mapper(componentModel = "spring")
public interface KeycloakDtoMapper {

    // ========== KeycloakGroupDto Mappings ==========

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "path", ignore = true)
    @Mapping(target = "subGroups", ignore = true)
    @Mapping(target = "attributes", ignore = true)
    @Mapping(target = "name", source = "name")
    KeycloakGroupDto toKeycloakGroupDto(String name);

    // ========== KeycloakResourceDto Mappings ==========

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "attributes", ignore = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "type", source = "resourceType")
    @Mapping(target = "displayName", source = "displayName")
    @Mapping(target = "owner", source = "ownerId")
    @Mapping(target = "ownerManagedAccess", source = "ownerManagedAccess")
    @Mapping(target = "uris", source = "uris")
    @Mapping(target = "scopes", source = "scopes")
    KeycloakResourceDto toKeycloakResourceDto(String name,
            String resourceType,
            String displayName,
            String ownerId,
            Boolean ownerManagedAccess,
            Set<String> uris,
            Set<String> scopes);

    // ========== KeycloakUserPolicyDto Mappings ==========

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "type", constant = "user")
    @Mapping(target = "logic", constant = "POSITIVE")
    @Mapping(target = "decisionStrategy", constant = "UNANIMOUS")
    @Mapping(target = "users", source = "users")
    KeycloakUserPolicyDto toKeycloakUserPolicyDto(String name, String description, Set<String> users);

    // ========== KeycloakPermissionDto Mappings ==========

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "type", constant = "scope")
    @Mapping(target = "logic", constant = "POSITIVE")
    @Mapping(target = "decisionStrategy", source = "decisionStrategy")
    @Mapping(target = "resources", source = "resources")
    @Mapping(target = "scopes", source = "scopes")
    @Mapping(target = "policies", source = "policies")
    KeycloakPermissionDto toKeycloakPermissionDto(String name,
            String description,
            String decisionStrategy,
            Set<String> resources,
            Set<String> scopes,
            Set<String> policies);

    // ========== KeycloakGroupPolicyDto Mappings ==========

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "groupsClaim", ignore = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "type", constant = "group")
    @Mapping(target = "logic", constant = "POSITIVE")
    @Mapping(target = "decisionStrategy", constant = "UNANIMOUS")
    @Mapping(target = "groups", source = "groups")
    KeycloakGroupPolicyDto toKeycloakGroupPolicyDto(String name,
            String description,
            List<KeycloakGroupPolicyDto.GroupDefinition> groups);

    @Mapping(target = "id", source = "groupId")
    @Mapping(target = "extendChildren", source = "extendChildren")
    KeycloakGroupPolicyDto.GroupDefinition toGroupDefinition(String groupId, Boolean extendChildren);

    // ========== Helper method for simple GroupDefinition ==========
    default KeycloakGroupPolicyDto.GroupDefinition toGroupDefinition(String groupId) {
        return toGroupDefinition(groupId, false);
    }
}
