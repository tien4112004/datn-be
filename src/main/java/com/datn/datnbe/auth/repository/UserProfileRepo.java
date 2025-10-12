package com.datn.datnbe.auth.repository;

import com.datn.datnbe.auth.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepo extends JpaRepository<UserProfile, String> {

    /**
     * Find user profile by Keycloak user ID
     */
    Optional<UserProfile> findByKeycloakUserId(String keycloakUserId);

    Optional<UserProfile> findByEmail(String email);
}
