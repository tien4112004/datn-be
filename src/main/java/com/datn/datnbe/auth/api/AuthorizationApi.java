package com.datn.datnbe.auth.api;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * API interface for authorization operations in Keycloak.
 * Provides methods for managing groups, policies, and permissions.
 */
public interface AuthorizationApi {

    /**
     * Creates or retrieves a Keycloak group and returns its ID.
     *
     * @param groupName the name of the group
     * @return the Keycloak group ID
     */
    String getOrCreateGroupId(String groupName);

    /**
     * Adds a user to a Keycloak group.
     *
     * @param keycloakUserId the Keycloak user ID
     * @param groupId the group ID
     */
    void addUserToGroup(String keycloakUserId, String groupId);

    /**
     * Removes a user from a Keycloak group.
     *
     * @param keycloakUserId the Keycloak user ID
     * @param groupId the group ID
     */
    void removeUserFromGroup(String keycloakUserId, String groupId);

    /**
     * Creates a group policy in Keycloak and returns its ID.
     *
     * @param name the policy name
     * @param description the policy description
     * @param groupIds the list of group IDs
     * @return the created group policy ID
     */
    String createGroupPolicy(String name, String description, List<String> groupIds);

    /**
     * Creates a permission in Keycloak.
     *
     * @param name the permission name
     * @param description the permission description
     * @param decisionStrategy the decision strategy
     * @param resourceIds the set of resource IDs
     * @param scopes the set of scopes
     * @param policyIds the set of policy IDs
     */
    void createPermission(String name,
            String description,
            String decisionStrategy,
            Set<String> resourceIds,
            Set<String> scopes,
            Set<String> policyIds);

    /**
     * Gets the Keycloak resource ID for a document.
     *
     * @param documentId the document ID
     * @return Optional containing the Keycloak resource ID, or empty if not found
     */
    Optional<String> getKeycloakResourceIdForDocument(String documentId);
}
