package com.datn.datnbe.auth.management;

import com.datn.datnbe.auth.api.UserProfileApi;
import com.datn.datnbe.auth.dto.request.SignupRequest;
import com.datn.datnbe.auth.dto.request.UserProfileUpdateRequest;
import com.datn.datnbe.auth.dto.response.UserProfileResponse;
import com.datn.datnbe.auth.entity.UserProfile;
import com.datn.datnbe.auth.mapper.UserProfileMapper;
import com.datn.datnbe.auth.repository.UserProfileRepo;
import com.datn.datnbe.auth.service.KeycloakAuthService;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginationDto;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserProfileManagement implements UserProfileApi {

    UserProfileRepo userProfileRepo;
    UserProfileRepo userProfileRepo;
    UserProfileMapper userProfileMapper;
    KeycloakAuthService keycloakAuthService;

    @Override
    public PaginatedResponseDto<UserProfileResponse> getUserProfiles(Pageable pageable) {
        var userProfiles = userProfileRepo.findAll(pageable)
                .getContent()
                .stream()
                .map(userProfileMapper::toResponseDto)
                .toList();
        var responseList = PaginationDto.getFromPageable(pageable);

        return PaginatedResponseDto.<UserProfileResponse>builder().data(userProfiles).pagination(responseList).build();
    }

    @Override
    @Transactional
    public UserProfileResponse createUserProfile(SignupRequest request) {
        log.info("Creating user profile for email: {}", request.getEmail());

        String keycloakUserId = null;

        try {
            keycloakUserId = keycloakAuthService.createKeycloakUser(request
                    .getEmail(), request.getPassword(), request.getFirstName(), request.getLastName(), "user");
                    .getEmail(), request.getPassword(), request.getFirstName(), request.getLastName(), "user");

            log.info("Successfully created user in Keycloak with ID: {}", keycloakUserId);
            UserProfile userProfile = userProfileMapper.toEntity(request);
            userProfile.setKeycloakUserId(keycloakUserId);

            UserProfile savedProfile = userProfileRepo.save(userProfile);
            UserProfile savedProfile = userProfileRepo.save(userProfile);

            log.info("Successfully created user profile in database with ID: {}", savedProfile.getId());
            UserProfileResponse response = userProfileMapper.toResponseDto(savedProfile);
            response.setEmail(request.getEmail());
            return response;

        } catch (Exception e) {
            if (keycloakUserId != null) {
                log.error("Failed to save user profile to database. Rolling back Keycloak user creation.", e);
                keycloakAuthService.deleteKeycloakUser(keycloakUserId);
            }

            log.error("Error creating user profile: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.USER_CREATION_FAILED);
        }
    }

    @Override
    public UserProfileResponseDto getUserProfileById(String userId) {
        log.debug("Retrieving user profile for user ID: {}", userId);

        UserProfile userProfile = userProfileRepo.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_PROFILE_NOT_FOUND,
                        "User profile not found for user ID: " + userId));

        UserProfileResponse response = userProfileMapper.toResponseDto(userProfile);

        // Fetch email from Keycloak
        try {
            String email = keycloakAuthService.getUserEmail(userProfile.getKeycloakUserId());
            response.setEmail(email);
        } catch (Exception e) {
            log.error("Failed to fetch email from Keycloak for user ID: {}", userId, e);
            // Continue without email
        }

        return response;
    }

    @Override
    @Transactional
    public UserProfileResponse updateUserProfile(String userId, UserProfileUpdateRequest request) {
        log.info("Updating user profile for user ID: {}", userId);

        UserProfile userProfile = userProfileRepo.findByIdOrKeycloakUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_PROFILE_NOT_FOUND,
                        "User profile not found for user ID: " + userId));

        // Update local user profile
        // Update local user profile
        userProfileMapper.updateEntityFromRequest(request, userProfile);
        UserProfile updatedProfile = userProfileRepo.save(userProfile);

        String keycloakUserId = userProfile.getKeycloakUserId();
        String currentEmail = null;

        // Sync with Keycloak
        try {
            // Get current email from Keycloak (since we don't store it locally)
            currentEmail = keycloakAuthService.getUserEmail(keycloakUserId);

            // Update Keycloak user with new names (keep existing email)
            keycloakAuthService.updateKeycloakUser(keycloakUserId,
                    updatedProfile.getFirstName(),
                    updatedProfile.getLastName(),
                    currentEmail);

            log.info("Successfully synced user profile update to Keycloak for user ID: {}", userId);
        } catch (Exception e) {
            log.error("Failed to sync profile update to Keycloak for user ID: {}. Error: {}", userId, e.getMessage());
            // Continue - local update succeeded, Keycloak sync failed but we don't rollback
        }
        UserProfile updatedProfile = userProfileRepo.save(userProfile);

        String keycloakUserId = userProfile.getKeycloakUserId();
        String currentEmail = null;

        // Sync with Keycloak
        try {
            // Get current email from Keycloak (since we don't store it locally)
            currentEmail = keycloakAuthService.getUserEmail(keycloakUserId);

            // Update Keycloak user with new names (keep existing email)
            keycloakAuthService.updateKeycloakUser(keycloakUserId,
                    updatedProfile.getFirstName(),
                    updatedProfile.getLastName(),
                    currentEmail);

            log.info("Successfully synced user profile update to Keycloak for user ID: {}", userId);
        } catch (Exception e) {
            log.error("Failed to sync profile update to Keycloak for user ID: {}. Error: {}", userId, e.getMessage());
            // Continue - local update succeeded, Keycloak sync failed but we don't rollback
        }

        log.info("Successfully updated user profile for user ID: {}", userId);
        UserProfileResponse response = userProfileMapper.toResponseDto(updatedProfile);
        response.setEmail(currentEmail);
        return response;
    }

    @Override
    @Transactional
    public void deleteUserProfile(String userId) {
        log.info("Deleting user profile for user ID: {}", userId);

        // Get the user profile first to retrieve the Keycloak user ID
        UserProfile userProfile = userProfileRepo.findByIdOrKeycloakUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_PROFILE_NOT_FOUND,
                        "User profile not found for user ID: " + userId));

        String keycloakUserId = userProfile.getKeycloakUserId();

        // Soft delete from local database (using @SQLDelete annotation)
        userProfileRepo.deleteById(userId);
        log.info("Successfully soft deleted user profile for user ID: {}", userId);

        // Delete from Keycloak
        try {
            keycloakAuthService.deleteKeycloakUser(keycloakUserId);
            log.info("Successfully deleted user from Keycloak: {}", keycloakUserId);
        } catch (Exception e) {
            log.error("Failed to delete user from Keycloak for user ID: {}. Error: {}", userId, e.getMessage());
            // Continue - local delete succeeded, consider if you want to throw or just log
        }
    }
}
