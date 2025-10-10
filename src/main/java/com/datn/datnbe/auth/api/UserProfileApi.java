package com.datn.datnbe.auth.api;

import org.springframework.data.domain.Pageable;

import com.datn.datnbe.auth.dto.request.SignupRequest;
import com.datn.datnbe.auth.dto.request.UserProfileUpdateRequest;
import com.datn.datnbe.auth.dto.response.UserProfileResponse;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;

public interface UserProfileApi {

    /**
     * Retrieves a list of user profiles based on the provided request criteria.
     *
     * @param pageable the Pageable object for pagination
     * @return List of UserProfileResponseDto containing the profile data
     */
    PaginatedResponseDto<UserProfileResponse> getUserProfiles(Pageable pageable);

    /**
     * Creates a new user profile.
     *
     * @param request the UserProfileCreateRequest containing the profile data
     * @return UserProfileResponseDto containing the created profile
     */
    UserProfileResponse createUserProfile(SignupRequest request);

    /**
     * Retrieves a user profile by user ID.
     *
     * @param userId the user ID
     * @return UserProfileResponseDto containing the profile data
     */
    UserProfileResponse getUserProfile(String userId);

    /**
     * Updates a user profile by user ID.
     *
     * @param userId  the user ID
     * @param request the UserProfileUpdateRequest containing the updated data
     * @return UserProfileResponseDto containing the updated profile
     */
    UserProfileResponse updateUserProfile(String userId, UserProfileUpdateRequest request);

    /**
     * Deletes a user profile by user ID.
     *
     * @param userId the user ID
     */
    void deleteUserProfile(String userId);
}
