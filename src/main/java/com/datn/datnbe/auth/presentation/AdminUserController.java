package com.datn.datnbe.auth.presentation;

import com.datn.datnbe.auth.api.UserProfileApi;
import com.datn.datnbe.auth.dto.request.UserProfileCreateRequest;
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
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminUserController {

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
        UserProfileResponseDto response = userProfileApi.getUserProfileByKeycloakId(userId);
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
}
