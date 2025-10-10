package com.datn.datnbe.auth.presentation;

import com.datn.datnbe.auth.api.UserProfileApi;
import com.datn.datnbe.auth.dto.request.UserProfileCreateRequest;
import com.datn.datnbe.auth.dto.request.UserProfileUpdateRequest;
import com.datn.datnbe.auth.dto.response.UserProfileResponseDto;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/user-profiles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserProfileController {

    UserProfileApi userProfileApi;

    /**
     * Endpoint to create a new user profile.
     *
     * @param request the UserProfileCreateRequest containing profile data
     * @return ResponseEntity containing the created user profile
     */
    @PostMapping
    public ResponseEntity<AppResponseDto<UserProfileResponseDto>> createUserProfile(
            @Valid @RequestBody UserProfileCreateRequest request) {
        UserProfileResponseDto response = userProfileApi.createUserProfile(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(AppResponseDto.success(response));
    }

    /**
     * Endpoint to get a user profile by Keycloak user ID.
     *
     * @param userId the Keycloak user ID
     * @return ResponseEntity containing the user profile
     */
    @GetMapping("/{userId}")
    public ResponseEntity<AppResponseDto<UserProfileResponseDto>> getUserProfile(@PathVariable String userId) {
        log.info("Fetching user profile for Keycloak user ID: {}", userId);
        UserProfileResponseDto response = userProfileApi.getUserProfileById(userId);
        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    /**
     * Endpoint to update a user profile by Keycloak user ID.
     *
     * @param userId the Keycloak user ID
     * @param request the UserProfileUpdateRequest containing updated data
     * @return ResponseEntity containing the updated user profile
     */
    @PatchMapping("/{userId}")
    public ResponseEntity<AppResponseDto<UserProfileResponseDto>> updateUserProfile(@PathVariable String userId,
            @Valid @RequestBody UserProfileUpdateRequest request) {
        log.info("Updating user profile for Keycloak user ID: {}", userId);
        UserProfileResponseDto response = userProfileApi.updateUserProfile(userId, request);
        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    /**
     * Endpoint to delete a user profile by Keycloak user ID.
     *
     * @param userId the Keycloak user ID
     * @return ResponseEntity with no content
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<AppResponseDto<Void>> deleteUserProfile(@PathVariable String userId) {
        log.info("Deleting user profile for Keycloak user ID: {}", userId);
        userProfileApi.deleteUserProfile(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint to check if a user profile exists for a given Keycloak user ID.
     *
     * @param userId the Keycloak user ID
     * @return ResponseEntity containing boolean result
     */
    @GetMapping("/{userId}/exists")
    public ResponseEntity<AppResponseDto<Boolean>> checkUserProfileExists(@PathVariable String userId) {
        log.info("Checking if user profile exists for Keycloak user ID: {}", userId);
        boolean exists = userProfileApi.existsById(userId);
        return ResponseEntity.ok(AppResponseDto.success(exists));
    }
}
