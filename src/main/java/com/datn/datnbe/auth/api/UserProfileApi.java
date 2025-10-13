package com.datn.datnbe.auth.api;

import com.datn.datnbe.auth.dto.request.UserProfileCreateRequest;
import com.datn.datnbe.auth.dto.request.UserProfileUpdateRequest;
import com.datn.datnbe.auth.dto.response.UserProfileResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import org.springframework.data.domain.Pageable;

public interface UserProfileApi {

    /**
     * Retrieves a list of user profiles based on the provided request criteria.
     *
     * @param pageable the Pageable object for pagination
     * @return List of UserProfileResponseDto containing the profile data
     */
    PaginatedResponseDto<UserProfileResponseDto> getUserProfiles(Pageable pageable);

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
    UserProfileResponseDto getUserProfile(String userId);

    /**
     * Updates a user profile by user ID.
     *
     * @param userId  the user ID
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
}
