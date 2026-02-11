package com.datn.datnbe.auth.management;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.datn.datnbe.auth.api.UserProfileApi;
import com.datn.datnbe.auth.dto.request.SignupRequest;
import com.datn.datnbe.auth.dto.request.UserCollectionRequest;
import com.datn.datnbe.auth.dto.request.UserProfileUpdateRequest;
import com.datn.datnbe.auth.dto.response.UpdateAvatarResponse;
import com.datn.datnbe.auth.dto.response.UserProfileResponse;
import com.datn.datnbe.auth.entity.UserProfile;
import com.datn.datnbe.auth.mapper.UserProfileMapper;
import com.datn.datnbe.auth.repository.UserProfileRepo;
import com.datn.datnbe.auth.service.KeycloakAuthService;
import com.datn.datnbe.auth.validation.AvatarValidation;
import com.datn.datnbe.payment.api.PaymentApi;
import com.datn.datnbe.document.api.MediaStorageApi;
import com.datn.datnbe.document.dto.response.UploadedMediaResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginationDto;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserProfileManagement implements UserProfileApi {

    UserProfileRepo userProfileRepo;
    UserProfileMapper userProfileMapper;
    KeycloakAuthService keycloakAuthService;
    MediaStorageApi mediaStorageApi;
    AvatarValidation avatarValidation;
    PaymentApi paymentApi;

    @Override
    public PaginatedResponseDto<UserProfileResponse> getUserProfiles(UserCollectionRequest request) {
        log.debug("Retrieving user profiles - page: {}, size: {}, search: {}",
                request.getPage(),
                request.getPageSize(),
                request.getSearch());

        Sort.Direction sortDirection = "desc".equalsIgnoreCase(request.getValidatedSort())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        Pageable pageable = PageRequest
                .of(request.getPage() - 1, request.getPageSize(), Sort.by(sortDirection, "createdAt"));

        Page<UserProfile> userProfilesPage;

        if (request.getSearch() != null && !request.getSearch().trim().isEmpty()) {
            userProfilesPage = userProfileRepo.findBySearchTerm(request.getSearch().trim(), pageable);
        } else {
            userProfilesPage = userProfileRepo.findAll(pageable);
        }

        var userProfiles = userProfilesPage.getContent().stream().map(userProfileMapper::toResponseDto).toList();

        PaginationDto pagination = new PaginationDto(request.getPage(), userProfilesPage.getSize(),
                userProfilesPage.getTotalElements(), userProfilesPage.getTotalPages());

        return PaginatedResponseDto.<UserProfileResponse>builder().data(userProfiles).pagination(pagination).build();
    }

    @Override
    @Transactional
    public UserProfileResponse createUserProfile(SignupRequest request) {
        String account = request.getEmail() != null ? request.getEmail() : request.getUsername();
        log.info("Creating user profile for account: {}", account);

        if (userProfileRepo.existsByEmail(account)) {
            log.error("User profile already exists for account: {}", account);
            throw new AppException(ErrorCode.RESOURCE_ALREADY_EXISTS,
                    "User with account '" + account + "' already exists in authentication system.");
        }

        String keycloakUserId = null;

        try {
            keycloakUserId = keycloakAuthService.createKeycloakUser(account,
                    request.getPassword(),
                    request.getFirstName(),
                    request.getLastName(),
                    "user");

            log.info("Successfully created user in Keycloak with ID: {}", keycloakUserId);
            UserProfile userProfile = userProfileMapper.toEntity(request);
            userProfile.setKeycloakUserId(keycloakUserId);

            UserProfile savedProfile = userProfileRepo.save(userProfile);

            log.info("Successfully created user profile in database with ID: {}", savedProfile.getId());
            UserProfileResponse response = userProfileMapper.toResponseDto(savedProfile);
            response.setEmail(request.getEmail());
            paymentApi.initializeUserCoin(savedProfile.getId());
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
    public UserProfileResponse getUserProfile(String userId) {
        log.debug("Retrieving user profile for user ID: {}", userId);
        UserProfile userProfile = userProfileRepo.findByIdOrKeycloakUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_PROFILE_NOT_FOUND,
                        "User profile not found for user ID: " + userId));

        return userProfileMapper.toResponseDto(userProfile);
    }

    @Override
    @Transactional
    public UserProfileResponse updateUserProfile(String userId, UserProfileUpdateRequest request) {
        log.info("Updating user profile for user ID: {}", userId);

        UserProfile userProfile = userProfileRepo.findByIdOrKeycloakUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_PROFILE_NOT_FOUND,
                        "User profile not found for user ID: " + userId));

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

    @Override
    @Transactional
    public void createUserFromKeycloakUser(String keycloakUserId, String email, String firstName, String lastName) {
        log.info("Creating user profile from Keycloak user ID: {}", keycloakUserId);

        try {
            // Check if user already exists
            if (userProfileRepo.findByKeycloakUserId(keycloakUserId).isPresent()) {
                log.info("User profile already exists for Keycloak user ID: {}. Skipping creation.", keycloakUserId);
                return;
            }

            UserProfile userProfile = UserProfile.builder()
                    .keycloakUserId(keycloakUserId)
                    .email(email)
                    .firstName(firstName)
                    .lastName(lastName)
                    .build();

            UserProfile savedProfile = userProfileRepo.save(userProfile);

            log.info("Successfully created user profile in database with ID: {}", savedProfile.getId());
            UserProfileResponse response = userProfileMapper.toResponseDto(savedProfile);
            response.setEmail(email);
            paymentApi.initializeUserCoin(savedProfile.getId());
        } catch (Exception e) {
            log.error("Error creating user profile from Keycloak user: {}, user might be existed", e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public UpdateAvatarResponse updateUserAvatar(String userId, MultipartFile avatar) {
        log.info("Updating avatar for user ID: {}", userId);

        // Validate avatar file (image only, max 5MB)
        avatarValidation.validateAvatarFile(avatar);

        // Find user profile
        UserProfile userProfile = userProfileRepo.findByIdOrKeycloakUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_PROFILE_NOT_FOUND, "User profile not found"));

        // Upload via media storage API (creates Media record automatically)
        UploadedMediaResponseDto uploadedMedia = mediaStorageApi.upload(avatar, userId);

        String newAvatarUrl = uploadedMedia.getCdnUrl();
        Long newMediaId = uploadedMedia.getId();

        log.info("Successfully uploaded avatar to media storage with ID: {}", newMediaId);

        // Delete old avatar if exists
        if (userProfile.getAvatarMediaId() != null) {
            try {
                mediaStorageApi.deleteMedia(userProfile.getAvatarMediaId());
                log.info("Successfully deleted old avatar with media ID: {}", userProfile.getAvatarMediaId());
            } catch (Exception e) {
                log.warn("Failed to delete old avatar, continuing with update: {}", e.getMessage());
            }
        }

        // Update user profile with new avatar URL and media ID
        userProfile.setAvatarUrl(newAvatarUrl);
        userProfile.setAvatarMediaId(newMediaId);
        userProfileRepo.save(userProfile);

        log.info("Successfully updated avatar for user ID: {}", userId);

        return UpdateAvatarResponse.builder().avatarUrl(newAvatarUrl).build();
    }

    @Override
    @Transactional
    public void removeUserAvatar(String userId) {
        log.info("Removing avatar for user ID: {}", userId);

        // Find user profile
        UserProfile userProfile = userProfileRepo.findByIdOrKeycloakUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_PROFILE_NOT_FOUND,
                        "User profile not found for user ID: " + userId));

        // Delete avatar from media storage if exists
        if (userProfile.getAvatarMediaId() != null) {
            try {
                mediaStorageApi.deleteMedia(userProfile.getAvatarMediaId());
                log.info("Successfully deleted avatar with media ID: {}", userProfile.getAvatarMediaId());
            } catch (Exception e) {
                log.error("Failed to delete avatar for user ID: {}. Error: {}", userId, e.getMessage());
                throw new AppException(ErrorCode.FILE_PROCESSING_ERROR,
                        "Failed to delete avatar from storage: " + e.getMessage());
            }
        } else {
            log.info("No avatar media ID found, skipping storage deletion");
        }

        // Update user profile to remove avatar URL and media ID
        userProfile.setAvatarUrl(null);
        userProfile.setAvatarMediaId(null);
        userProfileRepo.save(userProfile);

        log.info("Successfully removed avatar for user ID: {}", userId);
    }

    @Override
    @Transactional
    public UserProfileResponse createUserProfileByUsername(SignupRequest request) {
        log.info("Creating user profile for username: {}", request.getUsername());

        if (userProfileRepo.existsByEmail(request.getUsername())) {
            log.error("User profile already exists for username: {}", request.getUsername());
            throw new AppException(ErrorCode.RESOURCE_ALREADY_EXISTS,
                    "User with username '" + request.getUsername() + "' already exists in authentication system.");
        }

        String keycloakUserId = null;

        try {
            keycloakUserId = keycloakAuthService.createKeycloakUser(request
                    .getUsername(), request.getPassword(), request.getFirstName(), request.getLastName(), "user");

            log.info("Successfully created user in Keycloak with ID: {}", keycloakUserId);
            UserProfile userProfile = userProfileMapper.toEntity(request);
            userProfile.setKeycloakUserId(keycloakUserId);
            userProfile.setEmail(request.getUsername());

            UserProfile savedProfile = userProfileRepo.save(userProfile);

            log.info("Successfully created user profile in database with ID: {}", savedProfile.getId());
            UserProfileResponse response = userProfileMapper.toResponseDto(savedProfile);
            response.setUsername(request.getUsername());
            paymentApi.initializeUserCoin(savedProfile.getId());
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
    public com.datn.datnbe.auth.dto.response.UserMinimalInfoDto getUserMinimalInfo(String userId) {
        return userProfileRepo.findByIdOrKeycloakUserId(userId)
                .map(userProfile -> com.datn.datnbe.auth.dto.response.UserMinimalInfoDto.builder()
                        .id(userProfile.getId())
                        .firstName(userProfile.getFirstName())
                        .lastName(userProfile.getLastName())
                        .email(userProfile.getEmail())
                        .avatarUrl(userProfile.getAvatarUrl())
                        .build())
                .orElse(null);
    }

    @Override
    @Transactional
    public void updatePassword(String userId, String newPassword) {
        log.info("Updating password for user: {}", userId);

        UserProfile userProfile = userProfileRepo.findByIdOrKeycloakUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "User not found with ID: " + userId));

        try {
            // Update password via Keycloak
            keycloakAuthService.setUserPassword(userProfile.getKeycloakUserId(), newPassword);
            log.info("Password updated successfully for user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to update password for user {}: {}", userId, e.getMessage(), e);
            throw new AppException(ErrorCode.USER_UPDATE_FAILED, "Failed to update password");
        }
    }

    @Override
    public String getKeycloakUserIdByUserId(String userId) {
        return userProfileRepo.findByIdOrKeycloakUserId(userId).map(UserProfile::getKeycloakUserId).orElse(null);
    }
}
