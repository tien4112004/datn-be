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

import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {

    UserProfileRepo userProfileRepo;
    KeycloakAuthService keycloakAuthService;

    /**
     * Authenticate user with email and password and return tokens with user profile
     */
    public SignInResponse signIn(SigninRequest request) {
        log.info("Attempting to sign in user: {}", request.getEmail());

        var userProfileOpt = userProfileRepo.findByEmail(request.getEmail());
        if (userProfileOpt.isEmpty()) {
            log.error("User profile not found for email: {}", request.getEmail());
            throw new AppException(ErrorCode.USER_PROFILE_NOT_FOUND, "User not found");
        }

        try {
            var userKeycloakId = userProfileOpt.get().getKeycloakUserId();
            AuthTokenResponse tokenResponse = keycloakAuthService.signIn(request, userKeycloakId);
            if (tokenResponse == null || tokenResponse.getAccessToken() == null) {
                log.error("Failed to retrieve tokens from Keycloak for user: {}", request.getEmail());
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

    public void signOut(String refreshToken) {
        try {
            keycloakAuthService.signOut(refreshToken);
            log.info("User signed out successfully");
        } catch (Exception e) {
            log.error("Error during signout: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_ERROR, "Sign out failed: " + e.getMessage());
        }
    }
}
