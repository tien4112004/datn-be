package com.datn.datnbe.auth.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

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
    @Qualifier("keycloakAuthorizationRestTemplate")
    private final RestTemplate restTemplate;

    private final KeycloakAuthorizationProperties authzProperties;

    private final ObjectMapper objectMapper;

    // Cached service account token
    private String serviceAccountToken;
    private long tokenExpiryTime;

    private String getServiceAccountToken() {
        // Check if we have a valid cached token (with 30 second buffer)
        if (serviceAccountToken != null && System.currentTimeMillis() < tokenExpiryTime - 30000) {
            return serviceAccountToken;
        }

        log.debug("Requesting new service account token from Keycloak");

        String tokenUrl = String.format("%s/realms/%s/protocol/openid-connect/token",
                authzProperties.getServerUrl(),
                authzProperties.getRealm());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", authzProperties.getClientId());
        body.add("client_secret", authzProperties.getClientSecret());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<KeycloakTokenResponse> response = restTemplate
                    .postForEntity(tokenUrl, request, KeycloakTokenResponse.class);

            KeycloakTokenResponse tokenResponse = response.getBody();
            if (tokenResponse == null || tokenResponse.getAccessToken() == null) {
                throw new AppException(ErrorCode.UNAUTHORIZED, "Failed to obtain service account token");
            }

            serviceAccountToken = tokenResponse.getAccessToken();
            // Calculate expiry time (current time + expires_in seconds * 1000 for milliseconds)
            tokenExpiryTime = System.currentTimeMillis() + (tokenResponse.getExpiresIn() * 1000L);

            log.debug("Successfully obtained service account token, expires in {} seconds",
                    tokenResponse.getExpiresIn());

            return serviceAccountToken;

        } catch (HttpClientErrorException e) {
            log.error("Failed to obtain service account token: {}", e.getMessage());
            throw new AppException(ErrorCode.UNAUTHORIZED, "Failed to authenticate with Keycloak: " + e.getMessage());
        }
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(getServiceAccountToken());
        return headers;
    }

    public KeycloakResourceDto createResource(KeycloakResourceDto resource) {
        String url = String.format("%s/realms/%s/authz/protection/resource_set",
                authzProperties.getServerUrl(),
                authzProperties.getRealm());

        HttpHeaders headers = createAuthHeaders();
        HttpEntity<KeycloakResourceDto> request = new HttpEntity<>(resource, headers);

        try {
            log.debug("Creating resource in Keycloak: {}", resource.getName());

            // Keycloak returns just the resource ID as a JSON object like: {"_id": "resource-id"}
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            String responseBody = response.getBody();
            log.debug("Keycloak response: {}", responseBody);

            if (responseBody == null || responseBody.isEmpty()) {
                throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR,
                        "Failed to create resource: empty response from Keycloak");
            }

            // Parse the response to get the ID
            // Keycloak returns: {"_id": "some-uuid"}
            String resourceId = extractResourceIdFromResponse(responseBody);
            resource.setId(resourceId);

            log.info("Successfully created resource: {} with ID: {}", resource.getName(), resourceId);

            return resource;

        } catch (HttpClientErrorException e) {
            log.error("Failed to create resource: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "Failed to create resource in Keycloak: " + e.getMessage());
        }
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
        String url = String.format("%s/realms/%s/authz/protection/resource_set/%s",
                authzProperties.getServerUrl(),
                authzProperties.getRealm(),
                resourceId);

        HttpHeaders headers = createAuthHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            log.debug("Fetching resource from Keycloak: {}", resourceId);

            // Get response as String to handle Keycloak's JSON structure
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

            String responseBody = response.getBody();
            log.info("Keycloak getResource response: {}", responseBody);

            if (responseBody == null || responseBody.isEmpty()) {
                throw new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Empty response from Keycloak for resource: " + resourceId);
            }

            // Parse the JSON to extract resource details
            // Keycloak returns the full resource object for GET requests
            KeycloakResourceDto resource = parseResourceFromJson(responseBody, resourceId);

            if (resource.getOwner() == null) {
                log.error("Failed to extract owner from Keycloak response: {}", responseBody);
                throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR,
                        "Could not determine resource owner from Keycloak response");
            }

            return resource;

        } catch (HttpClientErrorException.NotFound e) {
            log.error("Resource not found: {}", resourceId);
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Resource not found in Keycloak: " + resourceId);
        } catch (HttpClientErrorException e) {
            log.error("Failed to fetch resource: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "Failed to fetch resource from Keycloak: " + e.getMessage());
        }
    }

    /**
     * Parses Keycloak's resource JSON response into KeycloakResourceDto.
     * Handles basic fields that we need from the response.
     */
    private KeycloakResourceDto parseResourceFromJson(String json, String expectedId) {
        try {
            // Extract key fields from JSON manually
            KeycloakResourceDto resource = new KeycloakResourceDto();

            // Extract _id
            String id = extractJsonField(json, "_id");
            resource.setId(id != null ? id : expectedId);

            // Extract name
            String name = extractJsonField(json, "name");
            resource.setName(name);

            // Extract owner - try different possible formats
            // Keycloak might return owner as string or as object with id field
            String owner = extractJsonField(json, "owner");
            if (owner == null) {
                // Try extracting from owner object: "owner":{"id":"..."}
                owner = extractNestedJsonField(json, "owner", "id");
            }
            if (owner == null) {
                // Try ownerManagedAccess field
                owner = extractJsonField(json, "ownerManagedAccess");
            }
            resource.setOwner(owner);

            // Extract type
            String type = extractJsonField(json, "type");
            resource.setType(type);

            log.debug("Parsed resource - id: {}, name: {}, owner: {}, type: {}", id, name, owner, type);
            return resource;

        } catch (Exception e) {
            log.error("Failed to parse Keycloak resource JSON: {}", json, e);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to parse Keycloak resource response");
        }
    }

    /**
     * Extracts a field value from JSON string.
     * Simple implementation for basic string fields.
     */
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

    /**
     * Extracts a nested field value from JSON string.
     * Example: extractNestedJsonField(json, "owner", "id") extracts "123" from {"owner":{"id":"123"}}
     */
    private String extractNestedJsonField(String json, String parentField, String childField) {
        try {
            // Pattern: "parentField":{..."childField":"value"...}
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

    /**
     * Parses Keycloak's user policy JSON response into KeycloakUserPolicyDto.
     */
    private KeycloakUserPolicyDto parseUserPolicyFromJson(String json) {
        try {
            KeycloakUserPolicyDto policy = new KeycloakUserPolicyDto();

            // Extract id
            String id = extractJsonField(json, "id");
            policy.setId(id);

            // Extract name
            String name = extractJsonField(json, "name");
            policy.setName(name);

            // Extract type
            String type = extractJsonField(json, "type");
            policy.setType(type);

            // Extract description (optional)
            String description = extractJsonField(json, "description");
            policy.setDescription(description);

            // Extract logic (could be "POSITIVE" or "NEGATIVE")
            String logic = extractJsonField(json, "logic");
            policy.setLogic(logic);

            // Extract decisionStrategy
            String decisionStrategy = extractJsonField(json, "decisionStrategy");
            policy.setDecisionStrategy(decisionStrategy);

            log.debug("Parsed user policy - id: {}, name: {}, type: {}", id, name, type);
            return policy;

        } catch (Exception e) {
            log.error("Failed to parse Keycloak user policy JSON: {}", json, e);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to parse Keycloak user policy response");
        }
    }

    /**
     * Parses Keycloak's permission JSON response into KeycloakPermissionDto.
     */
    private KeycloakPermissionDto parsePermissionFromJson(String json) {
        try {
            KeycloakPermissionDto permission = new KeycloakPermissionDto();

            // Extract id
            String id = extractJsonField(json, "id");
            permission.setId(id);

            // Extract name
            String name = extractJsonField(json, "name");
            permission.setName(name);

            // Extract type
            String type = extractJsonField(json, "type");
            permission.setType(type);

            // Extract description (optional)
            String description = extractJsonField(json, "description");
            permission.setDescription(description);

            // Extract logic
            String logic = extractJsonField(json, "logic");
            permission.setLogic(logic);

            // Extract decisionStrategy
            String decisionStrategy = extractJsonField(json, "decisionStrategy");
            permission.setDecisionStrategy(decisionStrategy);

            log.debug("Parsed permission - id: {}, name: {}, type: {}", id, name, type);
            return permission;

        } catch (Exception e) {
            log.error("Failed to parse Keycloak permission JSON: {}", json, e);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to parse Keycloak permission response");
        }
    }

    public void deleteResource(String resourceId) {
        String url = String.format("%s/realms/%s/authz/protection/resource_set/%s",
                authzProperties.getServerUrl(),
                authzProperties.getRealm(),
                resourceId);

        HttpHeaders headers = createAuthHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            log.debug("Deleting resource from Keycloak: {}", resourceId);

            restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);

            log.info("Successfully deleted resource: {}", resourceId);

        } catch (HttpClientErrorException e) {
            log.error("Failed to delete resource: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "Failed to delete resource from Keycloak: " + e.getMessage());
        }
    }

    public KeycloakUserPolicyDto createUserPolicy(KeycloakUserPolicyDto policy) {
        String url = String.format("%s/admin/realms/%s/clients/%s/authz/resource-server/policy/user",
                authzProperties.getServerUrl(),
                authzProperties.getRealm(),
                getClientUuid());

        HttpHeaders headers = createAuthHeaders();
        HttpEntity<KeycloakUserPolicyDto> request = new HttpEntity<>(policy, headers);

        try {
            log.debug("Creating user policy in Keycloak: {}", policy.getName());

            // Get response as String to handle Keycloak's JSON structure
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            String responseBody = response.getBody();
            log.info("Keycloak createUserPolicy response: {}", responseBody);

            if (responseBody == null || responseBody.isEmpty()) {
                throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR,
                        "Failed to create policy: empty response from Keycloak");
            }

            // Try to parse the response to extract policy ID
            KeycloakUserPolicyDto createdPolicy = null;
            try {
                createdPolicy = parseUserPolicyFromJson(responseBody);
            } catch (Exception e) {
                log.warn("Manual parsing failed, trying Jackson deserializer", e);
                // Fallback: try direct deserialization
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

            log.info("Successfully created user policy: {} with ID: {}",
                    createdPolicy.getName(),
                    createdPolicy.getId());

            return createdPolicy;

        } catch (HttpClientErrorException e) {
            log.error("Failed to create user policy: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "Failed to create policy in Keycloak: " + e.getMessage());
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error creating user policy: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "Unexpected error creating policy: " + e.getMessage());
        }
    }

    public KeycloakUserPolicyDto getPolicyByName(String policyName) {
        String url = String.format("%s/admin/realms/%s/clients/%s/authz/resource-server/policy?name=%s",
                authzProperties.getServerUrl(),
                authzProperties.getRealm(),
                getClientUuid(),
                policyName);

        HttpHeaders headers = createAuthHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            log.debug("Fetching policy from Keycloak: {}", policyName);

            ResponseEntity<List<KeycloakUserPolicyDto>> response = restTemplate
                    .exchange(url, HttpMethod.GET, request, new ParameterizedTypeReference<>() {
                    });

            List<KeycloakUserPolicyDto> policies = response.getBody();
            log.info("Keycloak getPolicyByName response for '{}': {}", policyName, policies);

            if (policies == null || policies.isEmpty()) {
                log.debug("No policy found with name: {}", policyName);
                return null;
            }

            log.info("Found policy: {} with ID: {}", policyName, policies.get(0).getId());
            return policies.get(0);

        } catch (HttpClientErrorException e) {
            log.error("Failed to fetch policy '{}': {} - {}",
                    policyName,
                    e.getStatusCode(),
                    e.getResponseBodyAsString());
            return null;
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

        String url = String.format("%s/admin/realms/%s/clients/%s/authz/resource-server/permission/scope",
                authzProperties.getServerUrl(),
                authzProperties.getRealm(),
                getClientUuid());

        HttpHeaders headers = createAuthHeaders();
        HttpEntity<KeycloakPermissionDto> request = new HttpEntity<>(permission, headers);

        try {
            log.debug("Creating permission in Keycloak: {}", permission.getName());

            // Get response as String to handle Keycloak's JSON structure
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            String responseBody = response.getBody();
            log.info("Keycloak createPermission response: {}", responseBody);

            if (responseBody == null || responseBody.isEmpty()) {
                throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR,
                        "Failed to create permission: empty response from Keycloak");
            }

            // Parse the response to extract permission details
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
        } catch (HttpClientErrorException e) {
            log.error("Failed to create permission: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "Failed to create permission in Keycloak: " + e.getMessage());
        }
    }

    public KeycloakPermissionDto getPermissionByName(String permissionName) {
        String url = String.format("%s/admin/realms/%s/clients/%s/authz/resource-server/permission?name=%s",
                authzProperties.getServerUrl(),
                authzProperties.getRealm(),
                getClientUuid(),
                permissionName);

        HttpHeaders headers = createAuthHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            log.debug("Fetching permission from Keycloak: {}", permissionName);

            ResponseEntity<List<KeycloakPermissionDto>> response = restTemplate
                    .exchange(url, HttpMethod.GET, request, new ParameterizedTypeReference<>() {
                    });

            List<KeycloakPermissionDto> permissions = response.getBody();
            log.info("Keycloak getPermissionByName response for '{}': {}", permissionName, permissions);

            if (permissions == null || permissions.isEmpty()) {
                log.debug("No permission found with name: {}", permissionName);
                return null;
            }

            KeycloakPermissionDto permission = permissions.get(0);

            // Fetch full permission details by ID to ensure all fields are populated
            if (permission.getId() != null) {
                permission = getPermissionById(permission.getId());
            }

            log.info("Found permission: {} with ID: {}, policies: {}, scopes: {}",
                    permissionName,
                    permission.getId(),
                    permission.getPolicies(),
                    permission.getScopes());
            return permission;

        } catch (HttpClientErrorException e) {
            log.error("Failed to fetch permission '{}': {} - {}",
                    permissionName,
                    e.getStatusCode(),
                    e.getResponseBodyAsString());
            return null;
        }
    }

    /**
     * Gets a permission by ID with full details.
     *
     * @param permissionId The permission ID
     * @return The permission with all fields populated
     */
    private KeycloakPermissionDto getPermissionById(String permissionId) {
        String url = String.format("%s/admin/realms/%s/clients/%s/authz/resource-server/permission/scope/%s",
                authzProperties.getServerUrl(),
                authzProperties.getRealm(),
                getClientUuid(),
                permissionId);

        HttpHeaders headers = createAuthHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            log.debug("Fetching permission by ID from Keycloak: {}", permissionId);

            ResponseEntity<KeycloakPermissionDto> response = restTemplate
                    .exchange(url, HttpMethod.GET, request, KeycloakPermissionDto.class);

            KeycloakPermissionDto permission = response.getBody();

            if (permission == null) {
                log.warn("Permission {} returned null from Keycloak", permissionId);
                return null;
            }

            log.debug("Fetched full permission details: ID={}, scopes={}, policies={}",
                    permission.getId(),
                    permission.getScopes(),
                    permission.getPolicies());

            return permission;

        } catch (HttpClientErrorException e) {
            log.error("Failed to fetch permission by ID '{}': {} - {}",
                    permissionId,
                    e.getStatusCode(),
                    e.getResponseBodyAsString());
            return null;
        }
    }

    public void updatePermission(String permissionId, KeycloakPermissionDto permission) {
        String url = String.format("%s/admin/realms/%s/clients/%s/authz/resource-server/permission/scope/%s",
                authzProperties.getServerUrl(),
                authzProperties.getRealm(),
                getClientUuid(),
                permissionId);

        HttpHeaders headers = createAuthHeaders();
        HttpEntity<KeycloakPermissionDto> request = new HttpEntity<>(permission, headers);

        try {
            log.debug("Updating permission in Keycloak: {} with policies: {}, scopes: {}",
                    permission.getName(),
                    permission.getPolicies(),
                    permission.getScopes());

            log.info("Sending permission update request: {}", permission);

            ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.PUT, request, Void.class);

            log.info("Keycloak updatePermission response status: {}", response.getStatusCode());
            log.info("Successfully updated permission: {} with policies: {}, scopes: {}",
                    permission.getName(),
                    permission.getPolicies(),
                    permission.getScopes());

        } catch (HttpClientErrorException e) {
            log.error("Failed to update permission '{}': {} - {}",
                    permission.getName(),
                    e.getStatusCode(),
                    e.getResponseBodyAsString());
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "Failed to update permission in Keycloak: " + e.getMessage());
        }
    }

    /**
     * Deletes a permission from Keycloak.
     *
     * @param permissionId The ID of the permission to delete
     * @throws AppException if deletion fails
     */
    public void deletePermission(String permissionId) {
        String url = String.format("%s/admin/realms/%s/clients/%s/authz/resource-server/permission/scope/%s",
                authzProperties.getServerUrl(),
                authzProperties.getRealm(),
                getClientUuid(),
                permissionId);

        HttpHeaders headers = createAuthHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            log.debug("Deleting permission from Keycloak: {}", permissionId);

            ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);

            log.info("Keycloak deletePermission response status: {}", response.getStatusCode());
            log.info("Successfully deleted permission: {}", permissionId);

        } catch (HttpClientErrorException e) {
            log.error("Failed to delete permission '{}': {} - {}",
                    permissionId,
                    e.getStatusCode(),
                    e.getResponseBodyAsString());
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "Failed to delete permission in Keycloak: " + e.getMessage());
        }
    }

    /**
     * Gets the client UUID for the configured client ID.
     * This is required for admin API calls.
     *
     * @return The client UUID
     */
    private String getClientUuid() {
        // In a production environment, you should cache this value
        // For now, we'll make an API call to get it
        String url = String.format("%s/admin/realms/%s/clients?clientId=%s",
                authzProperties.getServerUrl(),
                authzProperties.getRealm(),
                authzProperties.getClientId());

        HttpHeaders headers = createAuthHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<List<ClientRepresentation>> response = restTemplate
                    .exchange(url, HttpMethod.GET, request, new ParameterizedTypeReference<>() {
                    });

            List<ClientRepresentation> clients = response.getBody();
            if (clients == null || clients.isEmpty()) {
                throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR,
                        "Client not found: " + authzProperties.getClientId());
            }

            return clients.get(0).getId();

        } catch (HttpClientErrorException e) {
            log.error("Failed to fetch client UUID: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "Failed to fetch client UUID from Keycloak: " + e.getMessage());
        }
    }

    /**
     * Checks what permissions a user has on a specific resource using Keycloak's Token Endpoint.
     * This makes an authorization request to check if the user can access the resource.
     *
     * @param userToken The user's JWT access token
     * @param resourceName The Keycloak resource ID or name (ID is preferred, e.g., "798b7bdd-c2ba-4c64-8d48-60d68f65768a")
     * @return Set of permissions the user has (e.g., ["read", "write"])
     */
    public List<String> checkUserPermissions(String userToken, String resourceName) {
        log.info("Checking permissions for resource: {}", resourceName);

        String tokenUrl = String.format("%s/realms/%s/protocol/openid-connect/token",
                authzProperties.getServerUrl(),
                authzProperties.getRealm());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBearerAuth(userToken.replace("Bearer ", ""));

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "urn:ietf:params:oauth:grant-type:uma-ticket");
        body.add("audience", authzProperties.getClientId());

        // Request ALL possible scopes explicitly to get accurate permissions
        // Format: resourceId#scope (e.g., "798b7bdd-c2ba-4c64-8d48-60d68f65768a#read")
        body.add("permission", resourceName + "#read");
        body.add("permission", resourceName + "#write");
        body.add("permission", resourceName + "#share");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            log.info("Keycloak checkUserPermissions request - URL: {}, permission: {}, audience: {}",
                    tokenUrl,
                    resourceName,
                    authzProperties.getClientId());

            ResponseEntity<KeycloakTokenResponse> response = restTemplate
                    .exchange(tokenUrl, HttpMethod.POST, request, KeycloakTokenResponse.class);

            KeycloakTokenResponse tokenResponse = response.getBody();
            log.info("Keycloak checkUserPermissions response: status={}, hasToken={}",
                    response.getStatusCode(),
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

    /**
     * Extracts permissions (scopes) from the RPT (Requesting Party Token).
     * The RPT contains the authorization information in the JWT payload.
     */
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
            // The RPT contains: {"authorization": {"permissions": [{"rsname": "...", "scopes": ["read", "write"]}]}}
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
                // Check if this permission is for our resource
                com.fasterxml.jackson.databind.JsonNode rsidNode = permission.get("rsid");
                com.fasterxml.jackson.databind.JsonNode scopesNode = permission.get("scopes");

                if (rsidNode != null && rsidNode.asText().equals(resourceId) && scopesNode != null) {
                    // Extract all scopes for this resource
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

    /**
     * Simple representation of a Keycloak client for UUID lookup.
     */
    @lombok.Data
    private static class ClientRepresentation {
        private String id;
        private String clientId;
    }

    // ============================================
    // Group Management Methods
    // ============================================

    /**
     * Creates a group in Keycloak for file sharing.
     * If group already exists, returns the existing group.
     *
     * @param groupName The name of the group to create
     * @return The created or existing group with ID
     */
    public KeycloakGroupDto createGroup(String groupName) {
        // First check if group already exists
        KeycloakGroupDto existingGroup = getGroupByName(groupName);
        if (existingGroup != null) {
            log.info("Group {} already exists with ID: {}, returning existing group", groupName, existingGroup.getId());
            return existingGroup;
        }

        String url = String
                .format("%s/admin/realms/%s/groups", authzProperties.getServerUrl(), authzProperties.getRealm());

        KeycloakGroupDto group = KeycloakGroupDto.builder().name(groupName).build();

        HttpHeaders headers = createAuthHeaders();
        HttpEntity<KeycloakGroupDto> request = new HttpEntity<>(group, headers);

        try {
            log.debug("Creating group in Keycloak: {}", groupName);

            ResponseEntity<Void> response = restTemplate.postForEntity(url, request, Void.class);

            // Keycloak returns the group ID in the Location header
            String location = response.getHeaders().getLocation() != null
                    ? response.getHeaders().getLocation().toString()
                    : null;

            if (location == null) {
                throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to get group location from response");
            }

            // Extract group ID from location header (e.g., .../groups/group-id)
            String groupId = location.substring(location.lastIndexOf('/') + 1);
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
        } catch (HttpClientErrorException e) {
            log.error("Failed to create group: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "Failed to create group in Keycloak: " + e.getMessage());
        }
    }

    /**
     * Gets a group by name.
     *
     * @param groupName The name of the group
     * @return The group or null if not found
     */
    public KeycloakGroupDto getGroupByName(String groupName) {
        String url = String.format("%s/admin/realms/%s/groups?search=%s",
                authzProperties.getServerUrl(),
                authzProperties.getRealm(),
                groupName);

        HttpHeaders headers = createAuthHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            log.debug("Fetching group from Keycloak: {}", groupName);

            ResponseEntity<List<KeycloakGroupDto>> response = restTemplate
                    .exchange(url, HttpMethod.GET, request, new ParameterizedTypeReference<>() {
                    });

            List<KeycloakGroupDto> groups = response.getBody();

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

        } catch (HttpClientErrorException e) {
            log.error("Failed to fetch group '{}': {} - {}", groupName, e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        }
    }

    /**
     * Adds a user to a group.
     *
     * @param userId The user ID to add
     * @param groupId The group ID to add the user to
     */
    public void addUserToGroup(String userId, String groupId) {
        String url = String.format("%s/admin/realms/%s/users/%s/groups/%s",
                authzProperties.getServerUrl(),
                authzProperties.getRealm(),
                userId,
                groupId);

        HttpHeaders headers = createAuthHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            log.debug("Adding user {} to group {}", userId, groupId);

            restTemplate.exchange(url, HttpMethod.PUT, request, Void.class);

            log.info("Successfully added user {} to group {}", userId, groupId);

        } catch (HttpClientErrorException e) {
            log.error("Failed to add user to group: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "Failed to add user to group in Keycloak: " + e.getMessage());
        }
    }

    /**
     * Removes a user from a group.
     *
     * @param userId The user ID to remove
     * @param groupId The group ID to remove the user from
     */
    public void removeUserFromGroup(String userId, String groupId) {
        String url = String.format("%s/admin/realms/%s/users/%s/groups/%s",
                authzProperties.getServerUrl(),
                authzProperties.getRealm(),
                userId,
                groupId);

        HttpHeaders headers = createAuthHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            log.debug("Removing user {} from group {}", userId, groupId);

            restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);

            log.info("Successfully removed user {} from group {}", userId, groupId);

        } catch (HttpClientErrorException e) {
            log.error("Failed to remove user from group: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "Failed to remove user from group in Keycloak: " + e.getMessage());
        }
    }

    /**
     * Deletes a group from Keycloak.
     *
     * @param groupId The ID of the group to delete
     */
    public void deleteGroup(String groupId) {
        String url = String.format("%s/admin/realms/%s/groups/%s",
                authzProperties.getServerUrl(),
                authzProperties.getRealm(),
                groupId);

        HttpHeaders headers = createAuthHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            log.debug("Deleting group from Keycloak: {}", groupId);

            restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);

            log.info("Successfully deleted group: {}", groupId);

        } catch (HttpClientErrorException e) {
            log.error("Failed to delete group: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "Failed to delete group from Keycloak: " + e.getMessage());
        }
    }

    /**
     * Creates a group-based policy in Keycloak.
     * If policy already exists, returns the existing policy.
     *
     * @param policy The group policy to create
     * @return The created or existing policy with ID
     */
    public KeycloakGroupPolicyDto createGroupPolicy(KeycloakGroupPolicyDto policy) {
        // First check if policy already exists
        KeycloakGroupPolicyDto existingPolicy = getGroupPolicyByName(policy.getName());
        if (existingPolicy != null) {
            log.info("Group policy {} already exists with ID: {}, returning existing policy",
                    policy.getName(),
                    existingPolicy.getId());
            return existingPolicy;
        }

        String url = String.format("%s/admin/realms/%s/clients/%s/authz/resource-server/policy/group",
                authzProperties.getServerUrl(),
                authzProperties.getRealm(),
                getClientUuid());

        HttpHeaders headers = createAuthHeaders();
        HttpEntity<KeycloakGroupPolicyDto> request = new HttpEntity<>(policy, headers);

        try {
            log.debug("Creating group policy in Keycloak: {}", policy.getName());

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            String responseBody = response.getBody();
            log.info("Keycloak createGroupPolicy response: {}", responseBody);

            if (responseBody == null || responseBody.isEmpty()) {
                throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR,
                        "Failed to create group policy: empty response from Keycloak");
            }

            // Parse the response
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
        } catch (HttpClientErrorException e) {
            log.error("Failed to create group policy: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "Failed to create group policy in Keycloak: " + e.getMessage());
        }
    }

    /**
     * Parses Keycloak's group policy JSON response.
     */
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

    /**
     * Gets a group policy by name.
     *
     * @param policyName The name of the policy
     * @return The policy or null if not found
     */
    public KeycloakGroupPolicyDto getGroupPolicyByName(String policyName) {
        String url = String.format("%s/admin/realms/%s/clients/%s/authz/resource-server/policy?name=%s&type=group",
                authzProperties.getServerUrl(),
                authzProperties.getRealm(),
                getClientUuid(),
                policyName);

        HttpHeaders headers = createAuthHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            log.debug("Fetching group policy from Keycloak: {}", policyName);

            ResponseEntity<List<KeycloakGroupPolicyDto>> response = restTemplate
                    .exchange(url, HttpMethod.GET, request, new ParameterizedTypeReference<>() {
                    });

            List<KeycloakGroupPolicyDto> policies = response.getBody();

            if (policies == null || policies.isEmpty()) {
                log.debug("No group policy found with name: {}", policyName);
                return null;
            }

            log.info("Found group policy: {} with ID: {}", policyName, policies.get(0).getId());
            return policies.get(0);

        } catch (HttpClientErrorException e) {
            log.error("Failed to fetch group policy '{}': {} - {}",
                    policyName,
                    e.getStatusCode(),
                    e.getResponseBodyAsString());
            return null;
        }
    }
}
