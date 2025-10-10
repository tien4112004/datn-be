package com.datn.datnbe.auth.service;

import com.datn.datnbe.auth.utils.KeycloakUtils;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import jakarta.ws.rs.core.Response;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KeycloakAuthService {
    UsersResource usersResource;

    /**
     * Create user in Keycloak
     * Returns the Keycloak user ID
     */
    public String createKeycloakUser(String email, String password, String firstName, String lastName, String role) {
        try {

            // Create user representation
            UserRepresentation user = new UserRepresentation();
            user.setEnabled(true);
            user.setUsername(email);
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEmailVerified(false);

            // Set password
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(password);
            credential.setTemporary(false);
            user.setCredentials(Collections.singletonList(credential));

            // Set roles
            user.setRealmRoles(Collections.singletonList(role.toLowerCase()));

            // Create user
            Response response = usersResource.create(user);

            if (response.getStatus() == 201) {
                String locationHeader = response.getHeaderString("Location");
                String keycloakUserId = KeycloakUtils.extractUserIdFromLocation(locationHeader);
                log.info("Successfully created Keycloak user with ID: {}", keycloakUserId);
                return keycloakUserId;
            } else {
                log.error("Failed to create Keycloak user. Status: {}", response.getStatus());
                throw new AppException(ErrorCode.UNCATEGORIZED_ERROR,
                        "Failed to create user in Keycloak: " + response.getStatusInfo());
            }

        } catch (Exception e) {
            log.error("Error creating Keycloak user: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_ERROR, "Failed to create user in authentication system");
        }
    }

    /**
     * Update Keycloak user
     */
    public void updateKeycloakUser(String keycloakUserId, String firstName, String lastName, String email) {
        try {
            UserRepresentation user = usersResource.get(keycloakUserId).toRepresentation();
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEmail(email);

            usersResource.get(keycloakUserId).update(user);
            log.info("Successfully updated Keycloak user: {}", keycloakUserId);

        } catch (Exception e) {
            log.error("Error updating Keycloak user: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_ERROR, "Failed to update user in authentication system");
        }
    }

    /**
     * Delete Keycloak user (for rollback)
     */
    public void deleteKeycloakUser(String keycloakUserId) {
        try {
            usersResource.get(keycloakUserId).remove();
            log.info("Successfully deleted Keycloak user: {}", keycloakUserId);

        } catch (Exception e) {
            log.error("Error deleting Keycloak user: {}", e.getMessage(), e);
            // Don't throw here - this is cleanup
        }
    }
}
