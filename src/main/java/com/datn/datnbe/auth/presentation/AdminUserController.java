package com.datn.datnbe.auth.presentation;

import com.datn.datnbe.auth.api.UserProfileApi;
import com.datn.datnbe.auth.dto.request.SignupRequest;
import com.datn.datnbe.auth.dto.response.UserProfileResponse;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize(value = "hasRole('ADMIN')")
public class AdminUserController {

    UserProfileApi userProfileApi;

    /**
     * Endpoint to create a new user profile.
     *
     * @param request the UserProfileCreateRequest containing profile data
     * @return ResponseEntity containing the created user profile
     */
    @PostMapping
    public ResponseEntity<AppResponseDto<UserProfileResponse>> createUserProfile(
            @Valid @RequestBody SignupRequest request) {
        UserProfileResponse response = userProfileApi.createUserProfile(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(AppResponseDto.success(response));
    }

    /**
     * Endpoint to get a user profile by Keycloak user ID.
     *
     * @param userId the Keycloak user ID
     * @return ResponseEntity containing the user profile
     */
    @GetMapping("/{userId}")
    public ResponseEntity<AppResponseDto<UserProfileResponse>> getUserProfile(@PathVariable String userId) {
        log.info("Fetching user profile for Keycloak user ID: {}", userId);
        UserProfileResponse response = userProfileApi.getUserProfile(userId);
        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    /**
     * Endpoint to get all users.
     *
     * @return ResponseEntity containing the list of user profiles
     */
    @GetMapping("")
    public ResponseEntity<AppResponseDto> getAllUsers(Pageable pageable) {
        var response = userProfileApi.getUserProfiles(pageable);
        return ResponseEntity.ok(AppResponseDto.successWithPagination(response.getData(), response.getPagination()));
    }

    /**
     * Endpoint to delete a user profile by Keycloak user ID.
     *
     * @param userId the Keycloak user ID
     * @return ResponseEntity with no content
     */
    @Deprecated(forRemoval = true)
    @DeleteMapping("/{userId}")
    public ResponseEntity<AppResponseDto<Void>> deleteUserProfile(@PathVariable String userId) {
        log.info("Deleting user profile for Keycloak user ID: {}", userId);
        userProfileApi.deleteUserProfile(userId);
        return ResponseEntity.noContent().build();
    }
}
