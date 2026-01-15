package com.datn.datnbe.auth.presentation;

import com.datn.datnbe.auth.api.UserProfileApi;
import com.datn.datnbe.auth.dto.request.UserCollectionRequest;
import com.datn.datnbe.auth.dto.response.UserProfileResponse;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    UserProfileApi userProfileApi;

    /**
     * Endpoint to get all users with optional filtering.
     * Accessible to all authenticated users.
     *
     * @param request UserCollectionRequest containing pagination and filter parameters
     * @return ResponseEntity containing the list of user profiles
     */
    @GetMapping({"", "/"})
    public ResponseEntity<AppResponseDto<List<UserProfileResponse>>> getAllUsers(
            @Valid @ModelAttribute UserCollectionRequest request) {
        log.info("Fetching users - page: {}, size: {}, search: {}",
                request.getPage(),
                request.getPageSize(),
                request.getSearch());

        var response = userProfileApi.getUserProfiles(request);

        return ResponseEntity.ok(AppResponseDto.successWithPagination(response.getData(), response.getPagination()));
    }

    /**
     * Endpoint to get a user profile by user ID.
     * Accessible to all authenticated users.
     *
     * @param userId the user ID (database ID or Keycloak user ID)
     * @return ResponseEntity containing the user profile
     */
    @GetMapping("/{userId}")
    public ResponseEntity<AppResponseDto<UserProfileResponse>> getUserProfile(@PathVariable String userId) {
        log.info("Fetching user profile for user ID: {}", userId);
        UserProfileResponse response = userProfileApi.getUserProfile(userId);
        return ResponseEntity.ok(AppResponseDto.success(response));
    }
}
