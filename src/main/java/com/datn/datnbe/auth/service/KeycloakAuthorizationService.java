package com.datn.datnbe.auth.service;

import java.util.List;

import com.datn.datnbe.auth.dto.response.AuthTokenResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import com.datn.datnbe.auth.apiclient.KeycloakApiClient;
import com.datn.datnbe.auth.dto.keycloak.KeycloakGroupDto;
import com.datn.datnbe.auth.dto.keycloak.KeycloakGroupPolicyDto;
import com.datn.datnbe.auth.dto.keycloak.KeycloakPermissionDto;
import com.datn.datnbe.auth.dto.keycloak.KeycloakResourceDto;
import com.datn.datnbe.auth.dto.keycloak.KeycloakUserDto;
import com.datn.datnbe.auth.dto.keycloak.KeycloakUserPolicyDto;
import com.datn.datnbe.auth.mapper.KeycloakDtoMapper;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakAuthorizationService {

    private final KeycloakApiClient apiClient;
    private final KeycloakResponseParser responseParser;
    private final KeycloakDtoMapper keycloakMapper;

    // ========== Resource Management ==========

    public KeycloakResourceDto createResource(KeycloakResourceDto resource) {
        String responseBody = apiClient.createResource(resource);
        String resourceId = responseParser.extractResourceIdFromResponse(responseBody);
        resource.setId(resourceId);

        log.info("Successfully created resource: {} with ID: {}", resource.getName(), resourceId);
        return resource;
    }

    public KeycloakResourceDto getResource(String resourceId) {
        String responseBody = apiClient.getResource(resourceId);
        KeycloakResourceDto resource = responseParser.parseResourceFromJson(responseBody, resourceId);

        if (resource.getOwner() == null) {
            log.error("Failed to extract owner from Keycloak response: {}", responseBody);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "Could not determine resource owner from Keycloak response");
        }

        return resource;
    }

    // ========== Policy Management ==========

    public KeycloakUserPolicyDto createUserPolicy(KeycloakUserPolicyDto policy) {
        String responseBody = apiClient.createUserPolicy(policy);

        KeycloakUserPolicyDto createdPolicy = parseUserPolicyResponse(responseBody);

        if (createdPolicy == null || createdPolicy.getId() == null) {
            log.error("Failed to extract policy ID from response: {}", responseBody);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "Could not extract policy ID from Keycloak response");
        }

        log.info("Successfully created user policy: {} with ID: {}", createdPolicy.getName(), createdPolicy.getId());
        return createdPolicy;
    }

    private KeycloakUserPolicyDto parseUserPolicyResponse(String responseBody) {
        try {
            return responseParser.parseUserPolicyFromJson(responseBody);
        } catch (Exception e) {
            log.error("Failed to parse user policy response: {}", e.getMessage());
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "Failed to parse policy response: " + e.getMessage());
        }
    }

    public KeycloakGroupPolicyDto createGroupPolicy(KeycloakGroupPolicyDto policy) {
        KeycloakGroupPolicyDto existingPolicy = getGroupPolicyByName(policy.getName());
        if (existingPolicy != null) {
            log.info("Group policy {} already exists with ID: {}, returning existing policy",
                    policy.getName(),
                    existingPolicy.getId());
            return existingPolicy;
        }

        try {
            String responseBody = apiClient.createGroupPolicy(policy);
            KeycloakGroupPolicyDto createdPolicy = responseParser.parseGroupPolicyFromJson(responseBody);

            if (createdPolicy.getId() == null) {
                log.error("Failed to extract policy ID from response: {}", responseBody);
                throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR,
                        "Could not extract group policy ID from Keycloak response");
            }

            log.info("Successfully created group policy: {} with ID: {}",
                    createdPolicy.getName(),
                    createdPolicy.getId());
            return createdPolicy;

        } catch (HttpClientErrorException.Conflict e) {
            log.warn("Group policy {} already exists (conflict), fetching existing policy", policy.getName());
            existingPolicy = getGroupPolicyByName(policy.getName());
            if (existingPolicy != null) {
                return existingPolicy;
            }
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "Group policy exists but could not be retrieved: " + policy.getName());
        }
    }

    private KeycloakGroupPolicyDto getGroupPolicyByName(String policyName) {
        List<KeycloakGroupPolicyDto> policies = apiClient.searchGroupPolicies(policyName);

        if (policies == null || policies.isEmpty()) {
            log.debug("No group policy found with name: {}", policyName);
            return null;
        }

        log.info("Found group policy: {} with ID: {}", policyName, policies.getFirst().getId());
        return policies.getFirst();
    }

    // ========== Permission Management ==========

    public void createPermission(KeycloakPermissionDto permission) {
        KeycloakPermissionDto existingPermission = getPermissionByName(permission.getName());
        if (existingPermission != null) {
            log.info("Permission {} already exists with ID: {}, returning existing permission",
                    permission.getName(),
                    existingPermission.getId());
            return;
        }

        try {
            String responseBody = apiClient.createPermission(permission);
            KeycloakPermissionDto createdPermission = responseParser.parsePermissionFromJson(responseBody);

            if (createdPermission.getId() == null) {
                log.error("Failed to extract permission ID from response: {}", responseBody);
                throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR,
                        "Could not extract permission ID from Keycloak response");
            }

            log.info("Successfully created permission: {} with ID: {}",
                    createdPermission.getName(),
                    createdPermission.getId());

        } catch (HttpClientErrorException.Conflict e) {
            log.warn("Permission {} already exists (conflict), fetching existing permission", permission.getName());
            existingPermission = getPermissionByName(permission.getName());
            if (existingPermission != null) {
                return;
            }
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "Permission exists but could not be retrieved: " + permission.getName());
        }
    }

    public KeycloakPermissionDto getPermissionByName(String permissionName) {
        List<KeycloakPermissionDto> permissions = apiClient.getPermissionsByName(permissionName);

        log.info("Keycloak getPermissionByName response for '{}': {}", permissionName, permissions);

        if (permissions == null || permissions.isEmpty()) {
            log.debug("No permission found with name: {}", permissionName);
            return null;
        }

        KeycloakPermissionDto permission = permissions.getFirst();

        // Fetch full permission details by ID to ensure all fields are populated
        if (permission.getId() != null) {
            permission = apiClient.getPermissionById(permission.getId());
        }

        if (permission != null) {
            log.info("Found permission: {} with ID: {}, policies: {}, scopes: {}",
                    permissionName,
                    permission.getId(),
                    permission.getPolicies(),
                    permission.getScopes());
        }

        return permission;
    }

    // ========== Permission Check ==========

    public List<String> checkUserPermissions(String userId, String resourceId) {
        log.info("Checking permissions for resource: {} for user: {}", resourceId, userId);

        try {
            AuthTokenResponse tokenResponse = apiClient.requestRPT(userId, resourceId);

            log.info("Keycloak checkUserPermissions response: hasToken={}",
                    tokenResponse != null && tokenResponse.getAccessToken() != null);

            if (tokenResponse != null && tokenResponse.getAccessToken() != null) {
                log.info("User has access to resource: {}", resourceId);
                List<String> permissions = responseParser.extractPermissionsFromToken(tokenResponse.getAccessToken(),
                        resourceId);
                log.info("Extracted permissions from RPT: {}", permissions);
                return permissions;
            }

            log.info("User does not have access to resource: {} (empty token response)", resourceId);
            return List.of();

        } catch (HttpClientErrorException e) {
            log.info("Keycloak checkUserPermissions error response: {} - {}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString());
            if (e.getStatusCode() == HttpStatus.FORBIDDEN || e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                log.info("User does not have access to resource: {}", resourceId);
                return List.of();
            }

            log.error("Failed to check permissions: {}", e.getMessage());
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to check permissions: " + e.getMessage());
        } catch (AppException e) {
            log.error("Failed to check permissions: {}", e.getMessage());
            // If it's a Keycloak evaluation error (wrapped from RestClientException),
            // return empty permissions instead of throwing to fail gracefully
            if (e.getMessage() != null && e.getMessage().contains("Failed to evaluate permissions in Keycloak")) {
                log.warn("Keycloak permission evaluation failed, denying access to resource: {}", resourceId);
                return List.of();
            }
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while checking permissions for resource: {}", resourceId, e);
            // Fail gracefully - deny access rather than throw
            log.warn("Due to unexpected error, denying access to resource: {}", resourceId);
            return List.of();
        }
    }

    // ========== Group Management ==========

    public KeycloakGroupDto createGroup(String groupName) {
        KeycloakGroupDto existingGroup = getGroupByName(groupName);
        if (existingGroup != null) {
            log.info("Group {} already exists with ID: {}, returning existing group", groupName, existingGroup.getId());
            return existingGroup;
        }

        KeycloakGroupDto group = keycloakMapper.toKeycloakGroupDto(groupName);

        try {
            String groupId = apiClient.createGroup(group);
            group.setId(groupId);
            group.setPath("/" + groupName);

            log.info("Successfully created group: {} with ID: {}", groupName, groupId);
            return group;

        } catch (HttpClientErrorException.Conflict e) {
            log.warn("Group {} already exists (conflict), fetching existing group", groupName);
            existingGroup = getGroupByName(groupName);
            if (existingGroup != null) {
                return existingGroup;
            }
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "Group exists but could not be retrieved: " + groupName);
        }
    }

    private KeycloakGroupDto getGroupByName(String groupName) {
        List<KeycloakGroupDto> groups = apiClient.searchGroups(groupName);

        if (groups == null || groups.isEmpty()) {
            log.debug("No group found with name: {}", groupName);
            return null;
        }

        // Find exact match
        for (KeycloakGroupDto group : groups) {
            if (groupName.equals(group.getName())) {
                log.info("Found group: {} with ID: {}", groupName, group.getId());
                return group;
            }
        }

        log.debug("No exact match found for group: {}", groupName);
        return null;
    }

    public void addUserToGroup(String userId, String groupId) {
        apiClient.addUserToGroup(userId, groupId);
    }

    public void removeUserFromGroup(String userId, String groupId) {
        apiClient.removeUserFromGroup(userId, groupId);
    }

    public List<KeycloakUserDto> getGroupMembers(String groupId) {
        log.info("Fetching members for group: {}", groupId);
        return apiClient.getGroupMembers(groupId);
    }
}
