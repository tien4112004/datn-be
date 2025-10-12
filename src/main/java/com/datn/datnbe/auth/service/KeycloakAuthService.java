package com.datn.datnbe.auth.service;

import com.datn.datnbe.auth.config.properties.AuthProperties;
import com.datn.datnbe.auth.dto.request.SigninRequest;
import com.datn.datnbe.auth.dto.response.AuthTokenResponse;
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
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KeycloakAuthService {
    UsersResource usersResource;
    AuthProperties authProperties;
    WebClient webClient;

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

    public AuthTokenResponse signIn(SigninRequest request, String userKeycloakId) {
        log.info("url signin: {}", authProperties.getTokenUri());

        try {
            var requestBody = "client_id=" + authProperties.getClientId() + "&username=" + request.getEmail()
                    + "&password=" + request.getPassword() + "&grant_type=password" + "&client_secret="
                    + authProperties.getClientSecret() + "&user_id=" + userKeycloakId;

            return webClient.post()
                    .uri(authProperties.getTokenUri())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(status -> status.value() == 400,
                            resp -> resp.bodyToMono(String.class)
                                    .defaultIfEmpty("")
                                    .map(body -> new AppException(ErrorCode.AUTH_INVALID_CREDENTIALS, body)))
                    .onStatus(status -> status.value() == 401,
                            resp -> resp.bodyToMono(String.class)
                                    .defaultIfEmpty("")
                                    .map(body -> new AppException(ErrorCode.AUTH_UNAUTHORIZED, body)))
                    .onStatus(status -> status.value() >= 500,
                            resp -> resp.bodyToMono(String.class)
                                    .defaultIfEmpty("")
                                    .map(body -> new AppException(ErrorCode.AUTH_SERVER_ERROR, body)))
                    .bodyToMono(AuthTokenResponse.class)
                    .block();

        } catch (Exception e) {
            log.error("Error during Keycloak signin: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_ERROR, "Authentication failed: " + e.getMessage());
        }
    }

    /**
     * Get Keycloak user ID by email
     */
    public String getKeycloakUserIdByEmail(String email) {
        try {
            List<UserRepresentation> users = usersResource.searchByEmail(email, true);
            if (users.isEmpty()) {
                throw new AppException(ErrorCode.USER_PROFILE_NOT_FOUND, "User not found with email: " + email);
            }
            return users.get(0).getId();
        } catch (Exception e) {
            log.error("Error searching Keycloak user by email: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_ERROR, "Failed to find user by email");
        }
    }

    /**
     * Get user email from Keycloak
     */
    public String getUserEmail(String keycloakUserId) {
        try {
            UserRepresentation user = usersResource.get(keycloakUserId).toRepresentation();
            return user.getEmail();
        } catch (Exception e) {
            log.error("Error getting Keycloak user email: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_ERROR,
                    "Failed to retrieve user email from authentication system");
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
