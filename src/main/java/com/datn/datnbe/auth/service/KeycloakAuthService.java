package com.datn.datnbe.auth.service;

import java.util.Collections;

import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import com.datn.datnbe.auth.api.UserProfileApi;
import com.datn.datnbe.auth.config.AuthProperties;
import com.datn.datnbe.auth.dto.request.KeycloakCallbackRequest;
import com.datn.datnbe.auth.dto.request.SigninRequest;
import com.datn.datnbe.auth.dto.response.AuthTokenResponse;
import com.datn.datnbe.auth.utils.KeycloakUtils;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;

import jakarta.ws.rs.core.Response;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KeycloakAuthService {
    UsersResource usersResource;
    RealmResource realmResource;
    AuthProperties authProperties;
    WebClient webClient;
    RestTemplate restTemplate;

    @Lazy
    UserProfileApi userProfileApi;

    public KeycloakAuthService(UsersResource usersResource, RealmResource realmResource, AuthProperties authProperties,
            WebClient webClient, RestTemplate restTemplate, @Lazy UserProfileApi userProfileApi) {
        this.usersResource = usersResource;
        this.realmResource = realmResource;
        this.authProperties = authProperties;
        this.webClient = webClient;
        this.restTemplate = restTemplate;
        this.userProfileApi = userProfileApi;
    }

    /**
     * Create user in Keycloak
     * Returns the Keycloak user ID
     */
    public String createKeycloakUser(String account, String password, String firstName, String lastName, String role) {
        try {

            // Create user representation
            UserRepresentation user = new UserRepresentation();
            user.setEnabled(true);
            user.setUsername(account);
            if (account.contains("@")) {
                user.setEmail(account);
            }
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEmailVerified(true);

            // Set password
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(password);
            credential.setTemporary(false);
            user.setCredentials(Collections.singletonList(credential));

            // Create user
            Response response = usersResource.create(user);

            if (response.getStatus() == 201) {
                String locationHeader = response.getHeaderString("Location");
                String keycloakUserId = KeycloakUtils.extractUserIdFromLocation(locationHeader);
                log.info("Successfully created Keycloak user with ID: {}", keycloakUserId);

                // Assign realm role to the user
                assignRealmRole(keycloakUserId, role.toLowerCase());

                return keycloakUserId;
            } else {
                log.error("Failed to create Keycloak user. Status: {}", response.getStatus());
                throw new AppException(ErrorCode.UNCATEGORIZED_ERROR,
                        "Failed to create user in Keycloak: " + response.getStatusInfo());
            }

        } catch (Exception e) {
            log.error("Error creating Keycloak user: {}", e.getMessage(), e);
            // Rethrow
            throw e;
        }
    }

    /**
     * Assign realm role to a user
     */
    private void assignRealmRole(String keycloakUserId, String roleName) {
        try {
            // Get the role representation from the realm
            RoleRepresentation roleRepresentation = realmResource.roles().get(roleName).toRepresentation();

            // Assign the role to the user
            usersResource.get(keycloakUserId).roles().realmLevel().add(Collections.singletonList(roleRepresentation));

            log.info("Successfully assigned realm role '{}' to user: {}", roleName, keycloakUserId);

        } catch (Exception e) {
            log.error("Error assigning realm role '{}' to user {}: {}", roleName, keycloakUserId, e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_ERROR,
                    "Failed to assign role '" + roleName + "' to user. Make sure the role exists in Keycloak realm.");
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
     * Common method to exchange credentials with Keycloak token endpoint
     * Handles both password grant and authorization code grant
     */
    private AuthTokenResponse exchangeToken(String requestBody, String errorContext) {

        try {
            var response = webClient.post()
                    .uri(authProperties.getTokenUri())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(status -> status.value() == 400 || status.value() == 401,
                            resp -> resp.bodyToMono(String.class).defaultIfEmpty("").flatMap(body -> {
                                log.error("Authentication server error during {}: {}", errorContext, body);
                                return reactor.core.publisher.Mono
                                        .error(new AppException(ErrorCode.AUTH_INVALID_CREDENTIALS));
                            }))
                    .onStatus(status -> status.value() >= 500,
                            resp -> resp.bodyToMono(String.class)
                                    .defaultIfEmpty("")
                                    .flatMap(body -> reactor.core.publisher.Mono
                                            .error(new AppException(ErrorCode.AUTH_SERVER_ERROR, body))))
                    .bodyToMono(AuthTokenResponse.class)
                    .block();

            return response;
        } catch (AppException e) {
            log.debug("Authentication error during {}: {}", errorContext, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error during {}: {}", errorContext, e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_ERROR, "Authentication failed: " + e.getMessage());
        }
    }

    public AuthTokenResponse signIn(SigninRequest request, String userKeycloakId) {
        String account = request.getEmail() != null ? request.getEmail() : request.getUsername();
        log.info("url signin: {}", authProperties.getTokenUri());

        var requestBody = "client_id=" + authProperties.getClientId() + "&username=" + account + "&password="
                + request.getPassword() + "&grant_type=password" + "&client_secret=" + authProperties.getClientSecret()
                + "&user_id=" + userKeycloakId;

        return exchangeToken(requestBody, "Keycloak signin");
    }

    /**
     * Update Keycloak user password
     */
    public void setUserPassword(String keycloakUserId, String newPassword) {
        try {
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(newPassword);
            credential.setTemporary(false);

            usersResource.get(keycloakUserId).resetPassword(credential);
            log.info("Successfully reset password for Keycloak user: {}", keycloakUserId);

        } catch (Exception e) {
            log.error("Error resetting password for Keycloak user: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.USER_UPDATE_FAILED, "Failed to update password in authentication system");
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

    /**
     * Exchange Keycloak authorization code for JWT tokens
     * Used for OAuth2 Authorization Code flow with Identity Providers (Google, Facebook, etc.)
     *
     * @param request KeycloakCallbackRequest containing authorization code and redirect URI
     * @return AuthTokenResponse with access token, refresh token, and expiry information
     */
    public AuthTokenResponse exchangeAuthorizationCode(KeycloakCallbackRequest request) {
        log.info("Exchanging Keycloak authorization code for JWT tokens");

        var requestBody = "grant_type=authorization_code" + "&code=" + request.getCode() + "&redirect_uri="
                + request.getRedirectUri() + "&client_id=" + authProperties.getClientId() + "&client_secret="
                + authProperties.getClientSecret();

        return exchangeToken(requestBody, "authorization code exchange");
    }

    /**
     * Logout user by refresh token
     * Invalidates the session in Keycloak by sending the refresh token to the logout endpoint.
     */
    public void signOut(String refreshToken) {
        log.info("Processing logout with refresh token");
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", authProperties.getClientId());
        formData.add("client_secret", authProperties.getClientSecret());
        formData.add("refresh_token", refreshToken); // Crucial: Must be the Refresh Token, not Access Token

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);

        var logoutUrl = authProperties.getLogoutUri();
        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(logoutUrl, request, Void.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully logged out from Keycloak");
            } else {
                log.error("Keycloak logout failed. Status: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error calling Keycloak logout API", e);
            // if Keycloak is down, we still want to clear local cookies, so we might just log it.
        }

    }

}
