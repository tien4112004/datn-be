package com.datn.datnbe.sharedkernel.security.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import com.datn.datnbe.auth.entity.UserProfile;
import com.datn.datnbe.auth.repository.UserProfileRepo;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityContextUtils {

    private final UserProfileRepo userProfileRepo;

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
        UserProfile userProfile = userProfileRepo.findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED, "User is not authenticated"));
        return userProfile.getId();
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
}
