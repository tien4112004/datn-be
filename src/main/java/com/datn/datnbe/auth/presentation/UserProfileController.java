package com.datn.datnbe.auth.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.datn.datnbe.auth.api.UserProfileApi;
import com.datn.datnbe.auth.dto.request.UserProfileUpdateRequest;
import com.datn.datnbe.auth.dto.response.UserProfileResponse;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserProfileController {

    UserProfileApi userProfileApi;

    /**
     * Endpoint to get the current logged-in user's profile.
     *
     * @param jwt the JWT token containing the authenticated user's information
     * @return ResponseEntity containing the user profile
     */
    @GetMapping("/me")
    public ResponseEntity<AppResponseDto<UserProfileResponse>> getCurrentUserProfile(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        log.info("Fetching profile for current user with Keycloak user ID: {}", userId);
        UserProfileResponse response = userProfileApi.getUserProfile(userId);
        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    /**
     * Endpoint to delete the current logged-in user's profile.
     *
     * @param jwt the JWT token containing the authenticated user's information
     * @return ResponseEntity with no content
     */
    @DeleteMapping("/me")
    public ResponseEntity<AppResponseDto<Void>> deleteCurrentUserProfile(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        log.info("Deleting profile for current user with Keycloak user ID: {}", userId);
        userProfileApi.deleteUserProfile(userId);
        return ResponseEntity.ok(AppResponseDto.success());
    }

    /**
     * Endpoint to update a user profile by Keycloak user ID.
     *
     * @param jwt the JWT token containing the authenticated user's information
     * @param request the UserProfileUpdateRequest containing updated data
     * @return ResponseEntity containing the updated user profile
     */
    @Deprecated(forRemoval = true)
    @PatchMapping("/me")
    public ResponseEntity<AppResponseDto<UserProfileResponse>> updateUserProfile(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UserProfileUpdateRequest request) {
        String userId = jwt.getSubject();
        log.info("Updating user profile for Keycloak user ID: {}", userId);
        UserProfileResponse response = userProfileApi.updateUserProfile(userId, request);
        return ResponseEntity.ok(AppResponseDto.success(response));
    }
}
