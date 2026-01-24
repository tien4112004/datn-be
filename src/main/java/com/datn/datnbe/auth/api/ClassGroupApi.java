package com.datn.datnbe.auth.api;

import java.util.List;

/**
 * API interface for managing class groups in the authorization system.
 * Handles Keycloak group management for class-based access control.
 */
public interface ClassGroupApi {

    /**
     * Adds a user to a class group.
     *
     * @param classId the class ID
     * @param keycloakUserId the Keycloak user ID
     */
    void addUserToClassGroup(String classId, String keycloakUserId);

    /**
     * Removes a user from a class group.
     *
     * @param classId the class ID
     * @param keycloakUserId the Keycloak user ID
     */
    void removeUserFromClassGroup(String classId, String keycloakUserId);

    /**
     * Synchronizes class group members with the provided list of Keycloak user IDs.
     *
     * @param classId the class ID
     * @param keycloakUserIds the list of Keycloak user IDs to sync
     */
    void syncClassGroupMembers(String classId, List<String> keycloakUserIds);
}
