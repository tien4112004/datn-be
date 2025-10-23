package com.datn.datnbe.auth.utils;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.datn.datnbe.auth.dto.keycloak.KeycloakGroupPolicyDto;
import com.datn.datnbe.auth.dto.keycloak.KeycloakPermissionDto;
import com.datn.datnbe.auth.dto.keycloak.KeycloakResourceDto;
import com.datn.datnbe.auth.dto.keycloak.KeycloakUserPolicyDto;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public record KeycloakJsonParser(ObjectMapper objectMapper) {
    public String extractResourceIdFromResponse(String responseBody) {
        try {
            int startIndex = responseBody.indexOf("\"_id\"") + 7;
            int endIndex = responseBody.indexOf("\"", startIndex + 1);
            return responseBody.substring(startIndex, endIndex);
        } catch (Exception e) {
            log.error("Failed to parse resource ID from response: {}", responseBody, e);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to parse Keycloak response");
        }
    }

    public KeycloakResourceDto parseResourceFromJson(String json, String expectedId) {
        try {
            KeycloakResourceDto resource = new KeycloakResourceDto();
            resource.setId(extractJsonField(json, "_id", expectedId));
            resource.setName(extractJsonField(json, "name"));
            resource.setType(extractJsonField(json, "type"));

            // Try multiple ways to extract owner
            String owner = extractJsonField(json, "owner");
            if (owner == null) {
                owner = extractNestedJsonField(json, "owner", "id");
            }
            resource.setOwner(owner);

            log.debug("Parsed resource - id: {}, name: {}, owner: {}, type: {}",
                    resource.getId(),
                    resource.getName(),
                    owner,
                    resource.getType());
            return resource;
        } catch (Exception e) {
            log.error("Failed to parse Keycloak resource JSON: {}", json, e);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to parse Keycloak resource response");
        }
    }

    public KeycloakUserPolicyDto parseUserPolicyFromJson(String json) {
        try {
            KeycloakUserPolicyDto policy = new KeycloakUserPolicyDto();
            populateCommonPolicyFields(json, policy);

            log.debug("Parsed user policy - id: {}, name: {}, type: {}",
                    policy.getId(),
                    policy.getName(),
                    policy.getType());
            return policy;
        } catch (Exception e) {
            log.error("Failed to parse Keycloak user policy JSON: {}", json, e);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to parse Keycloak user policy response");
        }
    }

    public KeycloakPermissionDto parsePermissionFromJson(String json) {
        try {
            KeycloakPermissionDto permission = new KeycloakPermissionDto();
            populateCommonPolicyFields(json, permission);

            log.debug("Parsed permission - id: {}, name: {}, type: {}",
                    permission.getId(),
                    permission.getName(),
                    permission.getType());
            return permission;
        } catch (Exception e) {
            log.error("Failed to parse Keycloak permission JSON: {}", json, e);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to parse Keycloak permission response");
        }
    }

    public KeycloakGroupPolicyDto parseGroupPolicyFromJson(String json) {
        try {
            KeycloakGroupPolicyDto policy = new KeycloakGroupPolicyDto();
            populateCommonPolicyFields(json, policy);

            log.debug("Parsed group policy - id: {}, name: {}, type: {}",
                    policy.getId(),
                    policy.getName(),
                    policy.getType());
            return policy;
        } catch (Exception e) {
            log.error("Failed to parse Keycloak group policy JSON: {}", json, e);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to parse Keycloak group policy response");
        }
    }

    private void populateCommonPolicyFields(String json, Object policyObject) {
        // Extract all common fields once
        String id = extractJsonField(json, "id");
        String name = extractJsonField(json, "name");
        String type = extractJsonField(json, "type");
        String description = extractJsonField(json, "description");
        String logic = extractJsonField(json, "logic");
        String decisionStrategy = extractJsonField(json, "decisionStrategy");

        // Apply to the appropriate DTO type
        switch (policyObject) {
            case KeycloakUserPolicyDto policy -> {
                policy.setId(id);
                policy.setName(name);
                policy.setType(type);
                policy.setDescription(description);
                policy.setLogic(logic);
                policy.setDecisionStrategy(decisionStrategy);
            }
            case KeycloakPermissionDto permission -> {
                permission.setId(id);
                permission.setName(name);
                permission.setType(type);
                permission.setDescription(description);
                permission.setLogic(logic);
                permission.setDecisionStrategy(decisionStrategy);
            }
            case KeycloakGroupPolicyDto policy -> {
                policy.setId(id);
                policy.setName(name);
                policy.setType(type);
                policy.setDescription(description);
                policy.setLogic(logic);
                policy.setDecisionStrategy(decisionStrategy);
            }
            default -> {
                // No-op for unsupported types
            }
        }
    }

    public List<String> extractPermissionsFromToken(String rptToken, String resourceId) {
        try {
            String[] parts = rptToken.split("\\.");
            if (parts.length < 2) {
                log.warn("Invalid JWT token format");
                return List.of();
            }

            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            log.debug("RPT token payload: {}", payload);

            JsonNode rootNode = objectMapper.readTree(payload);
            JsonNode authorizationNode = rootNode.get("authorization");

            if (authorizationNode == null || authorizationNode.get("permissions") == null) {
                log.warn("No authorization/permissions found in RPT token");
                return List.of();
            }

            List<String> scopes = new ArrayList<>();
            JsonNode permissionsArray = authorizationNode.get("permissions");

            for (JsonNode permission : permissionsArray) {
                JsonNode rsidNode = permission.get("rsid");
                JsonNode scopesNode = permission.get("scopes");

                if (rsidNode != null && rsidNode.asText().equals(resourceId) && scopesNode != null) {
                    for (JsonNode scope : scopesNode) {
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

    private String extractJsonField(String json, String fieldName) {
        return extractJsonField(json, fieldName, null);
    }

    private String extractJsonField(String json, String fieldName, String defaultValue) {
        try {
            String pattern = "\"" + fieldName + "\"\\s*:\\s*\"([^\"]+)\"";
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(json);
            return m.find() ? m.group(1) : defaultValue;
        } catch (Exception e) {
            log.warn("Failed to extract field '{}' from JSON", fieldName);
            return defaultValue;
        }
    }

    private String extractNestedJsonField(String json, String parentField, String childField) {
        try {
            String pattern = "\"" + parentField + "\"\\s*:\\s*\\{[^}]*\"" + childField + "\"\\s*:\\s*\"([^\"]+)\"";
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(json);
            return m.find() ? m.group(1) : null;
        } catch (Exception e) {
            log.warn("Failed to extract nested field '{}.{}' from JSON", parentField, childField);
            return null;
        }
    }
}
