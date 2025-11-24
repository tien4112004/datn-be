package com.datn.datnbe.auth.service;

import java.util.Collections;
import java.util.List;

import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.datn.datnbe.auth.api.UserProfileApi;
import com.datn.datnbe.auth.config.AuthProperties;
import com.datn.datnbe.auth.dto.request.KeycloakCallbackRequest;
import com.datn.datnbe.auth.dto.request.SigninRequest;
import com.datn.datnbe.auth.dto.response.AuthTokenResponse;
import com.datn.datnbe.auth.dto.response.SignInResponse;
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

    @Lazy
    UserProfileApi userProfileApi;

    public KeycloakAuthService(UsersResource usersResource, RealmResource realmResource, AuthProperties authProperties,
            WebClient webClient, @Lazy UserProfileApi userProfileApi) {
        this.usersResource = usersResource;
        this.realmResource = realmResource;
        this.authProperties = authProperties;
        this.webClient = webClient;
        this.userProfileApi = userProfileApi;
    }

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
        log.info("url signin: {}", authProperties.getTokenUri());

        var requestBody = "client_id=" + authProperties.getClientId() + "&username=" + request.getEmail() + "&password="
                + request.getPassword() + "&grant_type=password" + "&client_secret=" + authProperties.getClientSecret()
                + "&user_id=" + userKeycloakId;

        final var result = exchangeToken(requestBody, "Keycloak signin");

        log.info("result: {}", result);

        return result;
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
     * Extract session ID from refresh token JWT
     * The refresh token contains 'sid' claim which is the session ID
     */
    public String extractSessionIdFromRefreshToken(String refreshToken) {
        try {
            JwtDecoder decoder = JwtDecoders.fromIssuerLocation(authProperties.getIssuer());
            Jwt jwt = decoder.decode(refreshToken);
            String sessionId = jwt.getClaimAsString("sid");

            if (sessionId == null || sessionId.isEmpty()) {
                log.warn("Session ID (sid) not found in refresh token");
                throw new AppException(ErrorCode.AUTH_INVALID_CREDENTIALS,
                        "Invalid refresh token: session ID not found");
            }

            log.debug("Extracted session ID from refresh token: {}", sessionId);
            return sessionId;
        } catch (Exception e) {
            log.error("Error extracting session ID from refresh token: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.AUTH_INVALID_CREDENTIALS, "Failed to parse refresh token");
        }
    }

    /**
     * Logout user by refresh token
     * Extracts session ID from refresh token and invalidates the session in Keycloak
     */
    public void signOut(String refreshToken) {
        log.info("Processing logout with refresh token");
        try {
            var requestBody = "client_id=" + authProperties.getClientId() + "&refresh_token=" + refreshToken
                    + "&client_secret=" + authProperties.getClientSecret();

            webClient.post()
                    .uri(authProperties.getLogoutUri())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(status -> status.value() >= 400,
                            resp -> resp.bodyToMono(String.class)
                                    .defaultIfEmpty("")
                                    .flatMap(body -> reactor.core.publisher.Mono.error(new AppException(
                                            ErrorCode.AUTH_SERVER_ERROR, "Failed to logout: " + body))))
                    .bodyToMono(String.class)
                    .block();

            log.info("Successfully invalidated session via refresh token");
        } catch (AppException e) {
            log.warn("Failed to invalidate session via refresh token: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.warn("Failed to invalidate session via refresh token: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.AUTH_SERVER_ERROR, "Failed to logout: " + e.getMessage());
        }
    }

    /**
     * Centralized method to process login callback from OAuth providers
     * Exchanges authorization code for tokens, decodes JWT, and syncs user profile
     *
     * @param request KeycloakCallbackRequest containing code and redirect URI
     * @return SignInResponse with tokens ready for cookie creation
     */
    public SignInResponse processLoginCallback(KeycloakCallbackRequest request) {
        log.info("Processing login callback - exchanging code for tokens");

        // Exchange authorization code for tokens
        AuthTokenResponse authTokenResponse = exchangeAuthorizationCode(request);

        // Decode JWT to get user info
        JwtDecoder decoder = JwtDecoders.fromIssuerLocation(authProperties.getIssuer());
        Jwt jwt = decoder.decode(authTokenResponse.getAccessToken());

        // Sync user profile if not exists
        userProfileApi.createUserFromKeycloakUser(jwt.getSubject(),
                jwt.getClaimAsString("email"),
                jwt.getClaimAsString("given_name"),
                jwt.getClaimAsString("family_name"));

        // Map AuthTokenResponse to SignInResponse
        return SignInResponse.builder()
                .accessToken(authTokenResponse.getAccessToken())
                .refreshToken(authTokenResponse.getRefreshToken())
                .tokenType(authTokenResponse.getTokenType())
                .expiresIn(authTokenResponse.getExpiresIn())
                .build();
    }

    /**
     * Generate Google login URL for OAuth authorization
     *
     * @param clientType Type of client (web, mobile, etc.)
     * @return Complete Google OAuth authorization URL
     */
    public String generateGoogleLoginUrl(String clientType) {
        log.info("Generating Google login URL for clientType: {}", clientType);

        // Encode client type in state parameter to track where to redirect after authentication
        String state = clientType + ":" + java.util.UUID.randomUUID().toString();

        java.util.Map<String, String> params = java.util.Map.of("client_id",
                authProperties.getClientId(),
                "response_type",
                "code",
                "scope",
                "openid profile email",
                "redirect_uri",
                authProperties.getGoogleCallbackUri(),
                "state",
                state,
                "kc_idp_hint",
                "google",
                "prompt",
                "login");

        String queryString = params.entrySet()
                .stream()
                .map(e -> e.getKey() + "="
                        + java.net.URLEncoder.encode(e.getValue(), java.nio.charset.StandardCharsets.UTF_8))
                .collect(java.util.stream.Collectors.joining("&"));

        return String.format("%s/realms/%s/protocol/openid-connect/auth?%s",
                authProperties.getServerUrl(),
                authProperties.getRealm(),
                queryString);
    }
}
