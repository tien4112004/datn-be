package com.datn.datnbe.auth.service;

import com.datn.datnbe.auth.api.UserProfileApi;
import com.datn.datnbe.auth.config.AuthProperties;
import com.datn.datnbe.auth.dto.request.KeycloakCallbackRequest;
import com.datn.datnbe.auth.dto.response.AuthTokenResponse;
import com.datn.datnbe.auth.dto.response.SignInResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for handling OAuth2 authorization code flow callbacks.
 * Manages the token exchange process and user profile synchronization.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthCallbackService {

    private final KeycloakAuthService keycloakAuthService;
    @Lazy
    private final UserProfileApi userProfileApi;
    private final AuthProperties authProperties;

    /**
     * Process OAuth callback by exchanging authorization code for tokens
     * and synchronizing user profile with our system
     *
     * @param request Callback request containing authorization code and redirect URI
     * @return SignInResponse with access and refresh tokens
     */
    public SignInResponse processCallback(KeycloakCallbackRequest request) {
        log.info("Processing OAuth callback - exchanging authorization code for tokens");

        // Exchange authorization code for tokens
        AuthTokenResponse authTokenResponse = keycloakAuthService.exchangeAuthorizationCode(request);

        // Decode JWT to get user information
        JwtDecoder decoder = JwtDecoders.fromIssuerLocation(authProperties.getIssuer());
        Jwt jwt = decoder.decode(authTokenResponse.getAccessToken());

        // Sync user profile if not exists in our database
        String keycloakUserId = jwt.getSubject();
        String email = jwt.getClaimAsString("email");
        String givenName = jwt.getClaimAsString("given_name");
        String familyName = jwt.getClaimAsString("family_name");

        log.debug("Syncing user profile for Keycloak user: {}", keycloakUserId);
        userProfileApi.createUserFromKeycloakUser(keycloakUserId, email, givenName, familyName);

        // Map AuthTokenResponse to SignInResponse
        return SignInResponse.builder()
                .accessToken(authTokenResponse.getAccessToken())
                .refreshToken(authTokenResponse.getRefreshToken())
                .tokenType(authTokenResponse.getTokenType())
                .expiresIn(authTokenResponse.getExpiresIn())
                .build();
    }

    /**
     * Generate OAuth authorization URL for Google login
     *
     * @param clientType Type of client requesting login (web, mobile, etc.)
     * @return Complete Google OAuth authorization URL
     */
    public String generateGoogleLoginUrl(String clientType) {
        log.info("Generating Google OAuth login URL for clientType: {}", clientType);

        // Encode client type in state parameter to track where to redirect after authentication
        String state = clientType + ":" + UUID.randomUUID();

        Map<String, String> params = Map.of("client_id",
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
                .collect(Collectors.joining("&"));

        String authUrl = String.format("%s/realms/%s/protocol/openid-connect/auth?%s",
                authProperties.getServerUrl(),
                authProperties.getRealm(),
                queryString);

        log.debug("Generated OAuth URL: {}", authUrl);
        return authUrl;
    }

    /**
     * Extract client type from OAuth state parameter
     *
     * @param state State parameter from OAuth callback
     * @return Client type (defaults to "web")
     */
    public String extractClientType(String state) {
        if (state != null && state.contains(":")) {
            return state.split(":")[0];
        }
        return "web";
    }
}
