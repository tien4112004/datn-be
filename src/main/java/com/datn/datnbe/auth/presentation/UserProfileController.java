package com.datn.datnbe.auth.presentation;

import com.datn.datnbe.auth.api.UserProfileApi;
import com.datn.datnbe.auth.dto.request.ChangePasswordRequest;
import com.datn.datnbe.auth.dto.request.SigninRequest;
import com.datn.datnbe.auth.dto.request.UserProfileUpdateRequest;
import com.datn.datnbe.auth.dto.response.UpdateAvatarResponse;
import com.datn.datnbe.auth.dto.response.UserProfileResponse;
import com.datn.datnbe.auth.service.AuthenticationService;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import com.datn.datnbe.sharedkernel.security.utils.SecurityContextUtils;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserProfileController {

    UserProfileApi userProfileApi;
    SecurityContextUtils securityContextUtils;
    AuthenticationService authenticationService;

    /**
     * Endpoint to get the current logged-in user's profile.
     *
     * @param authentication the Authentication object containing the authenticated user's information
     * @return ResponseEntity containing the user profile
     */
    @GetMapping("/me")
    public ResponseEntity<AppResponseDto<UserProfileResponse>> getCurrentUserProfile(Authentication authentication) {
        String userId = authentication.getName();
        log.info("Fetching profile for current user with Keycloak user ID: {}", userId);
        UserProfileResponse response = userProfileApi.getUserProfile(userId);
        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    /**
     * Endpoint to update a user profile by Keycloak user ID.
     *
     * @param jwt     the JWT token containing the authenticated user's information
     * @param request the UserProfileUpdateRequest containing updated data
     * @return ResponseEntity containing the updated user profile
     */
    @PatchMapping("/me")
    public ResponseEntity<AppResponseDto<UserProfileResponse>> updateUserProfile(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UserProfileUpdateRequest request) {
        String userId = jwt.getSubject();
        log.info("Updating user profile for Keycloak user ID: {}", userId);
        UserProfileResponse response = userProfileApi.updateUserProfile(userId, request);
        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    /**
     * Endpoint to update the current logged-in user's avatar.
     *
     * @param jwt  the JWT token containing the authenticated user's information
     * @param file the avatar image file (JPG, PNG, GIF). Maximum size 5MB.
     * @return ResponseEntity containing the URL of the updated avatar
     */
    @PostMapping(value = "/me/avatar", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<AppResponseDto<UpdateAvatarResponse>> upsertCurrentUserAvatar(
            @AuthenticationPrincipal Jwt jwt,
            MultipartFile file) {
        String userId = jwt.getSubject();
        log.info("Updating avatar for current user with Keycloak user ID: {}", userId);
        UpdateAvatarResponse response = userProfileApi.updateUserAvatar(userId, file);
        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    @DeleteMapping("/me/avatar")
    public ResponseEntity<AppResponseDto<Void>> removeCurrentUserAvatar(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        log.info("Deleting avatar for current user with Keycloak user ID: {}", userId);
        userProfileApi.removeUserAvatar(userId);
        return ResponseEntity.ok(AppResponseDto.success());
    }

    @PostMapping("/change-password")
    public ResponseEntity<AppResponseDto<Map<String, String>>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        // Get userId from security context
        String userId = securityContextUtils.getCurrentUserProfileId();

        // Validate userId is available
        if (userId == null || userId.isBlank()) {
            log.warn("Unable to get userId from security context for user: {}",
                    authentication != null ? authentication.getName() : "unknown");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AppResponseDto.<Map<String, String>>builder()
                            .success(false)
                            .code(401)
                            .data(Map.of("error", "User not authenticated properly"))
                            .build());
        }

        log.info("Received request to change password for user: {}", userId);
        UserProfileResponse user = userProfileApi.getUserProfile(userId);

        try {
            // Verify current password by attempting to sign in
            SigninRequest verifyRequest = SigninRequest.builder()
                    .username(user.getEmail())
                    .password(request.getCurrentPassword())
                    .build();

            // This will throw an exception if credentials are invalid
            authenticationService.signIn(verifyRequest);

            // Update password via UserProfileAPI
            userProfileApi.updatePassword(userId, request.getNewPassword());

            log.info("Password changed successfully for user: {}", userId);

            return ResponseEntity.ok(AppResponseDto.success(Map.of("message", "Password changed successfully")));

        } catch (Exception e) {
            log.error("Failed to change password for user {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(AppResponseDto.<Map<String, String>>builder()
                            .success(false)
                            .code(400)
                            .data(Map.of("error", "Invalid current password or failed to update password"))
                            .build());
        }
    }
}
