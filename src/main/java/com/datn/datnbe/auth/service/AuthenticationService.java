package com.datn.datnbe.auth.service;

import com.datn.datnbe.auth.dto.request.SigninRequest;
import com.datn.datnbe.auth.dto.response.AuthTokenResponse;
import com.datn.datnbe.auth.dto.response.SignInResponse;
import com.datn.datnbe.auth.repository.UserProfileRepo;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {

    UserProfileRepo userProfileRepo;
    KeycloakAuthService keycloakAuthService;
    SessionManagementService sessionService;

    /**
     * Authenticate user with email and password and return tokens with user profile
     */
    public SignInResponse signIn(SigninRequest request) {
        String account = request.getEmail() != null ? request.getEmail() : request.getUsername();
        log.info("Attempting to sign in user: {}", account);

        var userProfileOpt = userProfileRepo.findByEmail(account);
        if (userProfileOpt.isEmpty()) {
            log.error("User profile not found for username or email: {}", account);
            throw new AppException(ErrorCode.USER_PROFILE_NOT_FOUND, "User not found");
        }

        try {
            var userKeycloakId = userProfileOpt.get().getKeycloakUserId();
            AuthTokenResponse tokenResponse = keycloakAuthService.signIn(request, userKeycloakId);
            if (tokenResponse == null || tokenResponse.getAccessToken() == null) {
                log.error("Failed to retrieve tokens from Keycloak for user: {}", account);
                throw new AppException(ErrorCode.AUTH_INVALID_CREDENTIALS, "Authentication failed");
            }

            return SignInResponse.builder()
                    .accessToken(tokenResponse.getAccessToken())
                    .refreshToken(tokenResponse.getRefreshToken())
                    .expiresIn(tokenResponse.getExpiresIn())
                    .tokenType(tokenResponse.getTokenType())
                    .build();
        } catch (AppException e) {
            // Re-throw AppException as is - it already has the correct error code
            throw e;
        } catch (Exception e) {
            log.error("Error during signin: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_ERROR, "Authentication failed: " + e.getMessage());
        }
    }

    /**
     * Handles the complete logout process including local session invalidation
     * and Keycloak session invalidation.
     * Note: Cookie clearing is handled separately by SessionManagementService
     *
     * @param request  the HTTP request
     * @param response the HTTP response
     * @param auth     the current user's authentication
     */
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication auth) {
        // Invalidate local session
        new SecurityContextLogoutHandler().logout(request, response, auth);
        log.info("Local session invalidated for user: {}", auth != null ? auth.getName() : "unknown");

        // Extract refresh token from cookies
        final String refreshToken = sessionService.extractRefreshToken(request);
        if (refreshToken == null || refreshToken.isEmpty()) {
            log.warn("No refresh token found in cookies during logout.");
            throw new AppException(ErrorCode.UNCATEGORIZED_ERROR, "Invalid refresh token during logout");
        }

        // Logout from Keycloak
        keycloakAuthService.signOut(refreshToken);
        log.info("Successfully logged out from Keycloak");
    }
}
