package com.datn.datnbe.auth.management;

import com.datn.datnbe.auth.api.UserProfileApi;
import com.datn.datnbe.auth.dto.request.UserProfileCreateRequest;
import com.datn.datnbe.auth.dto.request.UserProfileUpdateRequest;
import com.datn.datnbe.auth.dto.response.UserProfileResponseDto;
import com.datn.datnbe.auth.entity.UserProfile;
import com.datn.datnbe.auth.mapper.UserProfileMapper;
import com.datn.datnbe.auth.repository.impl.jpa.UserProfileJPARepo;
import com.datn.datnbe.auth.service.KeycloakAuthService;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserProfileManagement implements UserProfileApi {

    UserProfileJPARepo userProfileJPARepo;
    UserProfileMapper userProfileMapper;
    KeycloakAuthService keycloakAuthService;

    @Override
    @Transactional
    public UserProfileResponseDto createUserProfile(UserProfileCreateRequest request) {
        log.info("Creating user profile for email: {}", request.getEmail());

        String keycloakUserId = null;

        try {
            keycloakUserId = keycloakAuthService.createKeycloakUser(request
                    .getEmail(), request.getPassword(), request.getFirstName(), request.getLastName(), "USER");

            log.info("Successfully created user in Keycloak with ID: {}", keycloakUserId);
            UserProfile userProfile = UserProfile.builder()
                    .keycloakUserId(keycloakUserId)
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .dateOfBirth(request.getDateOfBirth())
                    .build();

            UserProfile savedProfile = userProfileJPARepo.save(userProfile);

            log.info("Successfully created user profile in database with ID: {}", savedProfile.getId());
            return userProfileMapper.toResponseDto(savedProfile);

        } catch (Exception e) {
            // If saving to PostgreSQL fails, delete the Keycloak user
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

        UserProfile userProfile = userProfileJPARepo.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_PROFILE_NOT_FOUND,
                        "User profile not found for user ID: " + userId));

        return userProfileMapper.toResponseDto(userProfile);
    }

    @Override
    @Transactional
    public UserProfileResponseDto updateUserProfile(String userId, UserProfileUpdateRequest request) {
        log.info("Updating user profile for user ID: {}", userId);

        UserProfile userProfile = userProfileJPARepo.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_PROFILE_NOT_FOUND,
                        "User profile not found for user ID: " + userId));

        userProfileMapper.updateEntityFromRequest(request, userProfile);
        UserProfile updatedProfile = userProfileJPARepo.save(userProfile);

        log.info("Successfully updated user profile for user ID: {}", userId);
        return userProfileMapper.toResponseDto(updatedProfile);
    }

    @Override
    @Transactional
    public void deleteUserProfile(String userId) {
        log.info("Deleting user profile for user ID: {}", userId);
        userProfileJPARepo.deleteById(userId);
        log.info("Successfully deleted user profile for user ID: {}", userId);
    }

    @Override
    public boolean existsById(String userId) {
        return userProfileJPARepo.existsById(userId);
    }
}
