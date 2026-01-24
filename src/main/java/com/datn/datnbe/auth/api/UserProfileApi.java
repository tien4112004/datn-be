package com.datn.datnbe.auth.api;

import com.datn.datnbe.auth.dto.request.SignupRequest;
import com.datn.datnbe.auth.dto.request.UserCollectionRequest;
import com.datn.datnbe.auth.dto.request.UserProfileUpdateRequest;
import com.datn.datnbe.auth.dto.response.UpdateAvatarResponse;
import com.datn.datnbe.auth.dto.response.UserProfileResponse;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import org.springframework.web.multipart.MultipartFile;

public interface UserProfileApi {

    /**
     * Retrieves a list of user profiles based on the provided request criteria.
     *
     * @param request the UserCollectionRequest containing pagination and filter parameters
     * @return PaginatedResponseDto containing the list of UserProfileResponse
     */
    PaginatedResponseDto<UserProfileResponse> getUserProfiles(UserCollectionRequest request);

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

    /**
     * Creates a new user profile from Keycloak user information.
     *
     * @param keycloakUserId the Keycloak user ID
     * @param email the user's email
     * @param firstName the user's first name
     * @param lastName the user's last name
     */
    void createUserFromKeycloakUser(String keycloakUserId, String email, String firstName, String lastName);

    /**
     * Updates the avatar of a user profile.
     *
     * @param userId the user ID
     * @param avatar the avatar image file
     * @return UpdateAvatarResponse containing the CDN URL of the uploaded avatar
     */
    UpdateAvatarResponse updateUserAvatar(String userId, MultipartFile avatar);

    /**
     * Removes the avatar of a user profile.
     *
     * @param userId the user ID
     */
    void removeUserAvatar(String userId);

    UserProfileResponse createUserProfileByUsername(SignupRequest request);

    /**
     * Retrieves minimal user information by user ID or Keycloak user ID.
     *
     * @param userId the user ID or Keycloak user ID
     * @return UserMinimalInfoDto containing minimal user information, or null if not found
     */
    com.datn.datnbe.auth.dto.response.UserMinimalInfoDto getUserMinimalInfo(String userId);

    /**
     * Updates the password of a user profile.
     *
     * @param userId the user ID
     * @param newPassword the new password
     */
    void updatePassword(String userId, String newPassword);

    /**
     * Gets the Keycloak user ID for a given user ID.
     *
     * @param userId the user ID
     * @return the Keycloak user ID, or null if not found
     */
    String getKeycloakUserIdByUserId(String userId);
}
