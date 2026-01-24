package com.datn.datnbe.auth.management;

import com.datn.datnbe.auth.api.AuthorizationApi;
import com.datn.datnbe.auth.dto.keycloak.KeycloakGroupDto;
import com.datn.datnbe.auth.dto.keycloak.KeycloakGroupPolicyDto;
import com.datn.datnbe.auth.dto.keycloak.KeycloakPermissionDto;
import com.datn.datnbe.auth.mapper.KeycloakDtoMapper;
import com.datn.datnbe.auth.repository.DocumentResourceMappingRepository;
import com.datn.datnbe.auth.service.KeycloakAuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Management service for authorization operations in Keycloak.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorizationManagement implements AuthorizationApi {

    private final KeycloakAuthorizationService keycloakAuthzService;
    private final KeycloakDtoMapper keycloakMapper;
    private final DocumentResourceMappingRepository documentMappingRepository;

    @Override
    public String getOrCreateGroupId(String groupName) {
        KeycloakGroupDto group = keycloakAuthzService.createGroup(groupName);
        return group.getId();
    }

    @Override
    public void addUserToGroup(String keycloakUserId, String groupId) {
        keycloakAuthzService.addUserToGroup(keycloakUserId, groupId);
    }

    @Override
    public void removeUserFromGroup(String keycloakUserId, String groupId) {
        keycloakAuthzService.removeUserFromGroup(keycloakUserId, groupId);
    }

    @Override
    public String createGroupPolicy(String name, String description, List<String> groupIds) {
        List<KeycloakGroupPolicyDto.GroupDefinition> groups = groupIds.stream()
                .map(keycloakMapper::toGroupDefinition)
                .collect(Collectors.toList());

        KeycloakGroupPolicyDto policy = keycloakMapper.toKeycloakGroupPolicyDto(name, description, groups);
        KeycloakGroupPolicyDto createdPolicy = keycloakAuthzService.createGroupPolicy(policy);
        return createdPolicy.getId();
    }

    @Override
    public void createPermission(String name,
            String description,
            String decisionStrategy,
            Set<String> resourceIds,
            Set<String> scopes,
            Set<String> policyIds) {
        KeycloakPermissionDto permission = keycloakMapper
                .toKeycloakPermissionDto(name, description, decisionStrategy, resourceIds, scopes, policyIds);
        keycloakAuthzService.createPermission(permission);
    }

    @Override
    public Optional<String> getKeycloakResourceIdForDocument(String documentId) {
        return documentMappingRepository.findByDocumentId(documentId).map(mapping -> mapping.getKeycloakResourceId());
    }
}
