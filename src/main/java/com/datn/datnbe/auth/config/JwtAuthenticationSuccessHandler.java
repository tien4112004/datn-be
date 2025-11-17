package com.datn.datnbe.auth.config;

import com.datn.datnbe.auth.api.UserProfileApi;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Custom Authentication Success Handler to sync user profile from Keycloak JWT
 * when user logs in via OAuth2/OpenID Connect (Google, etc.)
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final UserProfileApi userProfileApi;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        try {
            // Extract JWT from authentication
            if (authentication.getPrincipal() instanceof Jwt jwt) {
                String userId = jwt.getSubject();
                String email = jwt.getClaimAsString("email");
                String givenName = jwt.getClaimAsString("given_name");
                String familyName = jwt.getClaimAsString("family_name");

                log.info("JWT Authentication Success - UserId: {}, Email: {}", userId, email);

                // Sync user profile if not exists
                userProfileApi.createUserFromKeycloakUser(userId, email, givenName, familyName);
            }
        } catch (Exception e) {
            log.error("Error syncing user profile during authentication: {}", e.getMessage(), e);
            // Don't throw - allow user to proceed even if sync fails
        }
    }
}
