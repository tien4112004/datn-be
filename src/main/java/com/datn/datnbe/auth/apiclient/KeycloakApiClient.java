package com.datn.datnbe.auth.apiclient;

import java.util.List;

import com.datn.datnbe.auth.dto.response.AuthTokenResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class KeycloakApiClient {

    @Qualifier("keycloakAuthorizationRestTemplate")
    private final RestTemplate restTemplate;

    private final KeycloakAuthorizationProperties authzProperties;

    private String serviceAccountToken;
    private long tokenExpiryTime;

    private String getServiceAccountToken() {
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

    public String createResource(KeycloakResourceDto resource) {
        String url = String.format("%s/realms/%s/authz/protection/resource_set",
                authzProperties.getServerUrl(),
                authzProperties.getRealm());

        HttpHeaders headers = createAuthHeaders();
        HttpEntity<KeycloakResourceDto> request = new HttpEntity<>(resource, headers);

        try {
            log.debug("Creating resource in Keycloak: {}", resource.getName());

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            String responseBody = response.getBody();

            log.debug("Keycloak response: {}", responseBody);

            if (responseBody == null || responseBody.isEmpty()) {
                throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR,
                        "Failed to create resource: empty response from Keycloak");
            }

            return responseBody;

        } catch (HttpClientErrorException e) {
            log.error("Failed to create resource: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "Failed to create resource in Keycloak: " + e.getMessage());
        }
    }

    public String getResource(String resourceId) {
        String url = String.format("%s/realms/%s/authz/protection/resource_set/%s",
                authzProperties.getServerUrl(),
                authzProperties.getRealm(),
                resourceId);

        HttpHeaders headers = createAuthHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            log.debug("Fetching resource from Keycloak: {}", resourceId);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            String responseBody = response.getBody();

            log.info("Keycloak getResource response: {}", responseBody);

            if (responseBody == null || responseBody.isEmpty()) {
                throw new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Empty response from Keycloak for resource: " + resourceId);
            }

            return responseBody;

        } catch (HttpClientErrorException.NotFound e) {
            log.error("Resource not found: {}", resourceId);
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Resource not found in Keycloak: " + resourceId);
        } catch (HttpClientErrorException e) {
            log.error("Failed to fetch resource: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "Failed to fetch resource from Keycloak: " + e.getMessage());
        }
    }

    public String createUserPolicy(KeycloakUserPolicyDto policy) {
        String url = String.format("%s/admin/realms/%s/clients/%s/authz/resource-server/policy/user",
                authzProperties.getServerUrl(),
                authzProperties.getRealm(),
                authzProperties.getClientUuid());

        HttpHeaders headers = createAuthHeaders();
        HttpEntity<KeycloakUserPolicyDto> request = new HttpEntity<>(policy, headers);

        try {
            log.debug("Creating user policy in Keycloak: {}", policy.getName());

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            String responseBody = response.getBody();

            log.info("Keycloak createUserPolicy response: {}", responseBody);

            if (responseBody == null || responseBody.isEmpty()) {
                throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR,
                        "Failed to create policy: empty response from Keycloak");
            }

            return responseBody;

        } catch (HttpClientErrorException e) {
            log.error("Failed to create user policy: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "Failed to create policy in Keycloak: " + e.getMessage());
        }
    }

    public String createPermission(KeycloakPermissionDto permission) {
        String url = String.format("%s/admin/realms/%s/clients/%s/authz/resource-server/permission/scope",
                authzProperties.getServerUrl(),
                authzProperties.getRealm(),
                authzProperties.getClientUuid());

        HttpHeaders headers = createAuthHeaders();
        HttpEntity<KeycloakPermissionDto> request = new HttpEntity<>(permission, headers);

        try {
            log.debug("Creating permission in Keycloak: {}", permission.getName());

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            String responseBody = response.getBody();

            log.info("Keycloak createPermission response: {}", responseBody);

            if (responseBody == null || responseBody.isEmpty()) {
                throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR,
                        "Failed to create permission: empty response from Keycloak");
            }

            return responseBody;

        } catch (HttpClientErrorException e) {
            throw e;
        }
    }

    public List<KeycloakPermissionDto> getPermissionsByName(String permissionName) {
        String url = String.format("%s/admin/realms/%s/clients/%s/authz/resource-server/permission?name=%s",
                authzProperties.getServerUrl(),
                authzProperties.getRealm(),
                authzProperties.getClientUuid(),
                permissionName);

        HttpHeaders headers = createAuthHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            log.debug("Fetching permission from Keycloak: {}", permissionName);

            ResponseEntity<List<KeycloakPermissionDto>> response = restTemplate
                    .exchange(url, HttpMethod.GET, request, new ParameterizedTypeReference<>() {
                    });

            return response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("Failed to fetch permission '{}': {} - {}",
                    permissionName,
                    e.getStatusCode(),
                    e.getResponseBodyAsString());
            return null;
        }
    }

    public KeycloakPermissionDto getPermissionById(String permissionId) {
        String url = String.format("%s/admin/realms/%s/clients/%s/authz/resource-server/permission/scope/%s",
                authzProperties.getServerUrl(),
                authzProperties.getRealm(),
                authzProperties.getClientUuid(),
                permissionId);

        HttpHeaders headers = createAuthHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            log.debug("Fetching permission by ID from Keycloak: {}", permissionId);

            ResponseEntity<KeycloakPermissionDto> response = restTemplate
                    .exchange(url, HttpMethod.GET, request, KeycloakPermissionDto.class);

            return response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("Failed to fetch permission by ID '{}': {} - {}",
                    permissionId,
                    e.getStatusCode(),
                    e.getResponseBodyAsString());
            return null;
        }
    }

    public AuthTokenResponse requestRPT(String userToken, String resourceName) {
        String tokenUrl = String.format("%s/realms/%s/protocol/openid-connect/token",
                authzProperties.getServerUrl(),
                authzProperties.getRealm());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBearerAuth(userToken.replace("Bearer ", ""));

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "urn:ietf:params:oauth:grant-type:uma-ticket");
        body.add("audience", authzProperties.getClientId());
        body.add("permission", resourceName + "#read");
        body.add("permission", resourceName + "#write");
        body.add("permission", resourceName + "#share");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            log.info("Requesting RPT for resource: {}", resourceName);

            ResponseEntity<AuthTokenResponse> response = restTemplate
                    .exchange(tokenUrl, HttpMethod.POST, request, AuthTokenResponse.class);

            return response.getBody();

        } catch (HttpClientErrorException e) {
            throw e;
        }
    }

    public String createGroup(KeycloakGroupDto group) {
        String url = String
                .format("%s/admin/realms/%s/groups", authzProperties.getServerUrl(), authzProperties.getRealm());

        HttpHeaders headers = createAuthHeaders();
        HttpEntity<KeycloakGroupDto> request = new HttpEntity<>(group, headers);

        try {
            log.debug("Creating group in Keycloak: {}", group.getName());

            ResponseEntity<Void> response = restTemplate.postForEntity(url, request, Void.class);

            String location = response.getHeaders().getLocation() != null
                    ? response.getHeaders().getLocation().toString()
                    : null;

            if (location == null) {
                throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to get group location from response");
            }

            // Extract group ID from location header
            return location.substring(location.lastIndexOf('/') + 1);

        } catch (HttpClientErrorException e) {
            throw e;
        }
    }

    public List<KeycloakGroupDto> searchGroups(String groupName) {
        String url = String.format("%s/admin/realms/%s/groups?search=%s",
                authzProperties.getServerUrl(),
                authzProperties.getRealm(),
                groupName);

        HttpHeaders headers = createAuthHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            log.debug("Searching for group: {}", groupName);

            ResponseEntity<List<KeycloakGroupDto>> response = restTemplate
                    .exchange(url, HttpMethod.GET, request, new ParameterizedTypeReference<>() {
                    });

            return response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("Failed to search groups: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        }
    }

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

    public String createGroupPolicy(KeycloakGroupPolicyDto policy) {
        String url = String.format("%s/admin/realms/%s/clients/%s/authz/resource-server/policy/group",
                authzProperties.getServerUrl(),
                authzProperties.getRealm(),
                authzProperties.getClientUuid());

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

            return responseBody;

        } catch (HttpClientErrorException e) {
            throw e;
        }
    }

    public List<KeycloakGroupPolicyDto> searchGroupPolicies(String policyName) {
        String url = String.format("%s/admin/realms/%s/clients/%s/authz/resource-server/policy?name=%s&type=group",
                authzProperties.getServerUrl(),
                authzProperties.getRealm(),
                authzProperties.getClientUuid(),
                policyName);

        HttpHeaders headers = createAuthHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            log.debug("Searching for group policy: {}", policyName);

            ResponseEntity<List<KeycloakGroupPolicyDto>> response = restTemplate
                    .exchange(url, HttpMethod.GET, request, new ParameterizedTypeReference<>() {
                    });

            return response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("Failed to search group policies: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        }
    }
}
