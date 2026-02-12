package com.datn.datnbe.sharedkernel.security.utils;

import com.datn.datnbe.auth.api.UserProfileApi;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityContextUtils {

    private final UserProfileApi userProfileApi;

    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "User is not authenticated");
        }

        // Handle JWT authentication (production)
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        }

        // Handle mock authentication (testing with @WithMockUser)
        if (authentication.getPrincipal() instanceof String username) {
            return username;
        }

        // Handle Spring Security User principal (testing)
        if (authentication
                .getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
            return userDetails.getUsername();
        }

        throw new AppException(ErrorCode.UNAUTHORIZED, "Invalid authentication principal");
    }

    public String getCurrentUserProfileId() {
        String keycloakUserId = getCurrentUserId();
        String userProfileId = userProfileApi.getUSerProfileIdByKeycloakUserId(keycloakUserId);

        if (userProfileId == null)
            throw new AppException(ErrorCode.UNAUTHORIZED,
                    "User profile not found for Keycloak user ID: " + keycloakUserId);

        return userProfileId;
    }

    public String getCurrentUserToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "User is not authenticated");
        }

        // Handle JWT authentication (production)
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getTokenValue();
        }

        // For test cases, return a mock token
        return "mock-test-token";
    }

    public Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public Jwt getCurrentJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "User is not authenticated");
        }

        if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt;
        }

        throw new AppException(ErrorCode.UNAUTHORIZED, "Invalid authentication principal");
    }

    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }

    public boolean hasRole(String role) {
        String keycloakUserId = getCurrentUserId();
        var userProfile = userProfileApi.getUserProfile(keycloakUserId);

        if (userProfile == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED,
                    "User profile not found for Keycloak user ID: " + keycloakUserId);
        }

        return userProfile.getRole().equalsIgnoreCase(role);
    }
}
