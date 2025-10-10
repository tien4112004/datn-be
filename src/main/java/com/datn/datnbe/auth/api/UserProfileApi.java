package com.datn.datnbe.auth.api;

import com.datn.datnbe.auth.dto.request.UserProfileCreateRequest;
import com.datn.datnbe.auth.dto.request.UserProfileUpdateRequest;
import com.datn.datnbe.auth.dto.response.UserProfileResponseDto;

public interface UserProfileApi {

    /**
     * Creates a new user profile.
     *
     * @param request the UserProfileCreateRequest containing the profile data
     * @return UserProfileResponseDto containing the created profile
     */
    UserProfileResponseDto createUserProfile(UserProfileCreateRequest request);

    /**
     * Retrieves a user profile by user ID.
     *
     * @param userId the user ID
     * @return UserProfileResponseDto containing the profile data
     */
    UserProfileResponseDto getUserProfileById(String userId);

    /**
     * Updates a user profile by user ID.
     *
     * @param userId the user ID
     * @param request the UserProfileUpdateRequest containing the updated data
     * @return UserProfileResponseDto containing the updated profile
     */
    UserProfileResponseDto updateUserProfile(String userId, UserProfileUpdateRequest request);

    /**
     * Deletes a user profile by user ID.
     *
     * @param userId the user ID
     */
    void deleteUserProfile(String userId);

    /**
     * Checks if a user profile exists for the given user ID.
     *
     * @param userId the user ID
     * @return true if a profile exists, false otherwise
     */
    boolean existsById(String userId);
}
