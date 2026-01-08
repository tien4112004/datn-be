package com.datn.datnbe.auth.presentation;

import com.datn.datnbe.auth.api.UserProfileApi;
import com.datn.datnbe.auth.dto.response.UserProfileResponse;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.data.domain.Pageable;
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
     * @param pageable pagination parameters
     * @param search optional search term to filter by name or email
     * @return ResponseEntity containing the list of user profiles
     */
    @GetMapping({"", "/"})
    public ResponseEntity<AppResponseDto<List<UserProfileResponse>>> getAllUsers(Pageable pageable,
            @RequestParam(required = false) String search) {
        log.info("Fetching users with search term: {}", search);

        var response = (search != null && !search.trim().isEmpty())
                ? userProfileApi.getUserProfiles(pageable, search.trim())
                : userProfileApi.getUserProfiles(pageable);

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
