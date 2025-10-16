package com.datn.datnbe.auth.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import com.datn.datnbe.auth.apiclient.KeycloakApiClient;
import com.datn.datnbe.auth.config.KeycloakAuthorizationProperties;
import com.datn.datnbe.auth.dto.keycloak.KeycloakGroupDto;
import com.datn.datnbe.auth.dto.keycloak.KeycloakGroupPolicyDto;
import com.datn.datnbe.auth.dto.keycloak.KeycloakPermissionDto;
import com.datn.datnbe.auth.dto.keycloak.KeycloakResourceDto;
import com.datn.datnbe.auth.dto.keycloak.KeycloakTokenResponse;
import com.datn.datnbe.auth.dto.keycloak.KeycloakUserPolicyDto;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakAuthorizationService {

    private final KeycloakApiClient apiClient;
    private final KeycloakAuthorizationProperties authzProperties;
    private final ObjectMapper objectMapper;

    private String cachedClientUuid;

    private String getClientUuid() {
        if (cachedClientUuid != null) {
            return cachedClientUuid;
        }

        cachedClientUuid = apiClient.getClientUuid(authzProperties.getClientId());
        return cachedClientUuid;
    }

    public KeycloakResourceDto createResource(KeycloakResourceDto resource) {
        String responseBody = apiClient.createResource(resource);
        String resourceId = extractResourceIdFromResponse(responseBody);
        resource.setId(resourceId);

        log.info("Successfully created resource: {} with ID: {}", resource.getName(), resourceId);
        return resource;
    }

    private String extractResourceIdFromResponse(String responseBody) {
        try {
            // Simple JSON parsing - Keycloak returns {"_id": "uuid"}
            int startIndex = responseBody.indexOf("\"_id\"") + 7;
            int endIndex = responseBody.indexOf("\"", startIndex + 1);
            return responseBody.substring(startIndex, endIndex);
        } catch (Exception e) {
            log.error("Failed to parse resource ID from response: {}", responseBody, e);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to parse Keycloak response");
        }
    }

    public KeycloakResourceDto getResource(String resourceId) {
        String responseBody = apiClient.getResource(resourceId);
        KeycloakResourceDto resource = parseResourceFromJson(responseBody, resourceId);

        if (resource.getOwner() == null) {
            log.error("Failed to extract owner from Keycloak response: {}", responseBody);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "Could not determine resource owner from Keycloak response");
        }

        return resource;
    }

    private KeycloakResourceDto parseResourceFromJson(String json, String expectedId) {
        try {
            KeycloakResourceDto resource = new KeycloakResourceDto();

            String id = extractJsonField(json, "_id");
            resource.setId(id != null ? id : expectedId);

            String name = extractJsonField(json, "name");
            resource.setName(name);

            String owner = extractJsonField(json, "owner");
            if (owner == null) {
                owner = extractNestedJsonField(json, "owner", "id");
            }
            if (owner == null) {
                owner = extractJsonField(json, "ownerManagedAccess");
            }
            resource.setOwner(owner);

            String type = extractJsonField(json, "type");
            resource.setType(type);

            log.debug("Parsed resource - id: {}, name: {}, owner: {}, type: {}", id, name, owner, type);
            return resource;

        } catch (Exception e) {
            log.error("Failed to parse Keycloak resource JSON: {}", json, e);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to parse Keycloak resource response");
        }
    }

    private String extractJsonField(String json, String fieldName) {
        try {
            String pattern = "\"" + fieldName + "\"\\s*:\\s*\"([^\"]+)\"";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);
            if (m.find()) {
                return m.group(1);
            }
            return null;
        } catch (Exception e) {
            log.warn("Failed to extract field '{}' from JSON", fieldName);
            return null;
        }
    }

    private String extractNestedJsonField(String json, String parentField, String childField) {
        try {
            String pattern = "\"" + parentField + "\"\\s*:\\s*\\{[^}]*\"" + childField + "\"\\s*:\\s*\"([^\"]+)\"";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);
            if (m.find()) {
                return m.group(1);
            }
            return null;
        } catch (Exception e) {
            log.warn("Failed to extract nested field '{}.{}' from JSON", parentField, childField);
            return null;
        }
    }

    public KeycloakUserPolicyDto createUserPolicy(KeycloakUserPolicyDto policy) {
        String clientUuid = getClientUuid();
        String responseBody = apiClient.createUserPolicy(policy, clientUuid);

        KeycloakUserPolicyDto createdPolicy = null;
        try {
            createdPolicy = parseUserPolicyFromJson(responseBody);
        } catch (Exception e) {
            log.warn("Manual parsing failed, trying Jackson deserializer", e);
            try {
                createdPolicy = objectMapper.readValue(responseBody, KeycloakUserPolicyDto.class);
            } catch (Exception ex) {
                log.error("Jackson parsing also failed: {}", ex.getMessage());
                throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR,
                        "Failed to parse policy response: " + ex.getMessage());
            }
        }

        if (createdPolicy == null || createdPolicy.getId() == null) {
            log.error("Failed to extract policy ID from response: {}", responseBody);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "Could not extract policy ID from Keycloak response");
        }

        log.info("Successfully created user policy: {} with ID: {}", createdPolicy.getName(), createdPolicy.getId());

        return createdPolicy;
    }

    private KeycloakUserPolicyDto parseUserPolicyFromJson(String json) {
        try {
            KeycloakUserPolicyDto policy = new KeycloakUserPolicyDto();

            String id = extractJsonField(json, "id");
            policy.setId(id);

            String name = extractJsonField(json, "name");
            policy.setName(name);

            String type = extractJsonField(json, "type");
            policy.setType(type);

            String description = extractJsonField(json, "description");
            policy.setDescription(description);

            String logic = extractJsonField(json, "logic");
            policy.setLogic(logic);

            String decisionStrategy = extractJsonField(json, "decisionStrategy");
            policy.setDecisionStrategy(decisionStrategy);

            log.debug("Parsed user policy - id: {}, name: {}, type: {}", id, name, type);
            return policy;

        } catch (Exception e) {
            log.error("Failed to parse Keycloak user policy JSON: {}", json, e);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to parse Keycloak user policy response");
        }
    }

    public KeycloakPermissionDto createPermission(KeycloakPermissionDto permission) {
        // First check if permission already exists
        KeycloakPermissionDto existingPermission = getPermissionByName(permission.getName());
        if (existingPermission != null) {
            log.info("Permission {} already exists with ID: {}, returning existing permission",
                    permission.getName(),
                    existingPermission.getId());
            return existingPermission;
        }

        String clientUuid = getClientUuid();

        try {
            String responseBody = apiClient.createPermission(permission, clientUuid);
            KeycloakPermissionDto createdPermission = parsePermissionFromJson(responseBody);

            if (createdPermission.getId() == null) {
                log.error("Failed to extract permission ID from response: {}", responseBody);
                throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR,
                        "Could not extract permission ID from Keycloak response");
            }

            log.info("Successfully created permission: {} with ID: {}",
                    createdPermission.getName(),
                    createdPermission.getId());

            return createdPermission;

        } catch (HttpClientErrorException.Conflict e) {
            // Permission already exists, fetch and return it
            log.warn("Permission {} already exists (conflict), fetching existing permission", permission.getName());
            existingPermission = getPermissionByName(permission.getName());
            if (existingPermission != null) {
                return existingPermission;
            }
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "Permission exists but could not be retrieved: " + permission.getName());
        }
    }

    private KeycloakPermissionDto parsePermissionFromJson(String json) {
        try {
            KeycloakPermissionDto permission = new KeycloakPermissionDto();

            String id = extractJsonField(json, "id");
            permission.setId(id);

            String name = extractJsonField(json, "name");
            permission.setName(name);

            String type = extractJsonField(json, "type");
            permission.setType(type);

            String description = extractJsonField(json, "description");
            permission.setDescription(description);

            String logic = extractJsonField(json, "logic");
            permission.setLogic(logic);

            String decisionStrategy = extractJsonField(json, "decisionStrategy");
            permission.setDecisionStrategy(decisionStrategy);

            log.debug("Parsed permission - id: {}, name: {}, type: {}", id, name, type);
            return permission;

        } catch (Exception e) {
            log.error("Failed to parse Keycloak permission JSON: {}", json, e);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to parse Keycloak permission response");
        }
    }

    public KeycloakPermissionDto getPermissionByName(String permissionName) {
        String clientUuid = getClientUuid();
        List<KeycloakPermissionDto> permissions = apiClient.getPermissionsByName(permissionName, clientUuid);

        log.info("Keycloak getPermissionByName response for '{}': {}", permissionName, permissions);

        if (permissions == null || permissions.isEmpty()) {
            log.debug("No permission found with name: {}", permissionName);
            return null;
        }

        KeycloakPermissionDto permission = permissions.get(0);

        // Fetch full permission details by ID to ensure all fields are populated
        if (permission.getId() != null) {
            permission = apiClient.getPermissionById(permission.getId(), clientUuid);
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

    public void updatePermission(String permissionId, KeycloakPermissionDto permission) {
        String clientUuid = getClientUuid();

        log.debug("Updating permission in Keycloak: {} with policies: {}, scopes: {}",
                permission.getName(),
                permission.getPolicies(),
                permission.getScopes());

        log.info("Sending permission update request: {}", permission);

        apiClient.updatePermission(permissionId, permission, clientUuid);

        log.info("Successfully updated permission: {} with policies: {}, scopes: {}",
                permission.getName(),
                permission.getPolicies(),
                permission.getScopes());
    }

    public List<String> checkUserPermissions(String userToken, String resourceName) {
        log.info("Checking permissions for resource: {}", resourceName);

        try {
            KeycloakTokenResponse tokenResponse = apiClient.requestRPT(userToken, resourceName);

            log.info("Keycloak checkUserPermissions response: hasToken={}",
                    tokenResponse != null && tokenResponse.getAccessToken() != null);

            if (tokenResponse != null && tokenResponse.getAccessToken() != null) {
                // User has access - parse the RPT (Requesting Party Token) to get actual permissions
                log.info("User has access to resource: {}", resourceName);

                // Decode the RPT token to extract permissions
                List<String> permissions = extractPermissionsFromToken(tokenResponse.getAccessToken(), resourceName);
                log.info("Extracted permissions from RPT: {}", permissions);

                return permissions;
            }

            log.info("User does not have access to resource: {} (empty token response)", resourceName);
            return List.of();

        } catch (HttpClientErrorException e) {
            log.info("Keycloak checkUserPermissions error response: {} - {}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString());
            if (e.getStatusCode() == HttpStatus.FORBIDDEN || e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                log.info("User does not have access to resource: {}", resourceName);
                return List.of(); // No permissions
            }

            log.error("Failed to check permissions: {}", e.getMessage());
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to check permissions: " + e.getMessage());
        }
    }

    private List<String> extractPermissionsFromToken(String rptToken, String resourceId) {
        try {
            // JWT tokens have 3 parts separated by dots: header.payload.signature
            String[] parts = rptToken.split("\\.");
            if (parts.length < 2) {
                log.warn("Invalid JWT token format");
                return List.of();
            }

            // Decode the payload (second part)
            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            log.debug("RPT token payload: {}", payload);

            // Parse the JSON to extract authorization information
            com.fasterxml.jackson.databind.JsonNode rootNode = objectMapper.readTree(payload);
            com.fasterxml.jackson.databind.JsonNode authorizationNode = rootNode.get("authorization");

            if (authorizationNode == null || authorizationNode.get("permissions") == null) {
                log.warn("No authorization/permissions found in RPT token");
                return List.of();
            }

            // Extract scopes for our resource
            List<String> scopes = new java.util.ArrayList<>();
            com.fasterxml.jackson.databind.JsonNode permissionsArray = authorizationNode.get("permissions");

            for (com.fasterxml.jackson.databind.JsonNode permission : permissionsArray) {
                com.fasterxml.jackson.databind.JsonNode rsidNode = permission.get("rsid");
                com.fasterxml.jackson.databind.JsonNode scopesNode = permission.get("scopes");

                if (rsidNode != null && rsidNode.asText().equals(resourceId) && scopesNode != null) {
                    for (com.fasterxml.jackson.databind.JsonNode scope : scopesNode) {
                        scopes.add(scope.asText());
                    }
                }
            }

            return scopes.isEmpty() ? List.of() : scopes;

        } catch (Exception e) {
            log.error("Failed to parse RPT token: {}", e.getMessage(), e);
            return List.of();
        }
    }

    public KeycloakGroupDto createGroup(String groupName) {
        // First check if group already exists
        KeycloakGroupDto existingGroup = getGroupByName(groupName);
        if (existingGroup != null) {
            log.info("Group {} already exists with ID: {}, returning existing group", groupName, existingGroup.getId());
            return existingGroup;
        }

        KeycloakGroupDto group = KeycloakGroupDto.builder().name(groupName).build();

        try {
            String groupId = apiClient.createGroup(group);
            group.setId(groupId);
            group.setPath("/" + groupName);

            log.info("Successfully created group: {} with ID: {}", groupName, groupId);
            return group;

        } catch (HttpClientErrorException.Conflict e) {
            // Group already exists (race condition or retry), fetch and return it
            log.warn("Group {} already exists (conflict), fetching existing group", groupName);
            existingGroup = getGroupByName(groupName);
            if (existingGroup != null) {
                return existingGroup;
            }
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "Group exists but could not be retrieved: " + groupName);
        }
    }

    public KeycloakGroupDto getGroupByName(String groupName) {
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

    public KeycloakGroupPolicyDto createGroupPolicy(KeycloakGroupPolicyDto policy) {
        // First check if policy already exists
        KeycloakGroupPolicyDto existingPolicy = getGroupPolicyByName(policy.getName());
        if (existingPolicy != null) {
            log.info("Group policy {} already exists with ID: {}, returning existing policy",
                    policy.getName(),
                    existingPolicy.getId());
            return existingPolicy;
        }

        String clientUuid = getClientUuid();

        try {
            String responseBody = apiClient.createGroupPolicy(policy, clientUuid);
            KeycloakGroupPolicyDto createdPolicy = parseGroupPolicyFromJson(responseBody);

            if (createdPolicy == null || createdPolicy.getId() == null) {
                log.error("Failed to extract policy ID from response: {}", responseBody);
                throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR,
                        "Could not extract group policy ID from Keycloak response");
            }

            log.info("Successfully created group policy: {} with ID: {}",
                    createdPolicy.getName(),
                    createdPolicy.getId());

            return createdPolicy;

        } catch (HttpClientErrorException.Conflict e) {
            // Policy already exists, fetch and return it
            log.warn("Group policy {} already exists (conflict), fetching existing policy", policy.getName());
            existingPolicy = getGroupPolicyByName(policy.getName());
            if (existingPolicy != null) {
                return existingPolicy;
            }
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "Group policy exists but could not be retrieved: " + policy.getName());
        }
    }

    private KeycloakGroupPolicyDto parseGroupPolicyFromJson(String json) {
        try {
            KeycloakGroupPolicyDto policy = new KeycloakGroupPolicyDto();

            String id = extractJsonField(json, "id");
            policy.setId(id);

            String name = extractJsonField(json, "name");
            policy.setName(name);

            String type = extractJsonField(json, "type");
            policy.setType(type);

            String description = extractJsonField(json, "description");
            policy.setDescription(description);

            String logic = extractJsonField(json, "logic");
            policy.setLogic(logic);

            String decisionStrategy = extractJsonField(json, "decisionStrategy");
            policy.setDecisionStrategy(decisionStrategy);

            log.debug("Parsed group policy - id: {}, name: {}, type: {}", id, name, type);
            return policy;

        } catch (Exception e) {
            log.error("Failed to parse Keycloak group policy JSON: {}", json, e);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to parse Keycloak group policy response");
        }
    }

    public KeycloakGroupPolicyDto getGroupPolicyByName(String policyName) {
        String clientUuid = getClientUuid();
        List<KeycloakGroupPolicyDto> policies = apiClient.searchGroupPolicies(policyName, clientUuid);

        if (policies == null || policies.isEmpty()) {
            log.debug("No group policy found with name: {}", policyName);
            return null;
        }

        log.info("Found group policy: {} with ID: {}", policyName, policies.get(0).getId());
        return policies.get(0);
    }
}
