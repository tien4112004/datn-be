package com.datn.datnbe.auth.repository;

import com.datn.datnbe.auth.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepo
        extends
            JpaRepository<UserProfile, String>,
            PagingAndSortingRepository<UserProfile, String> {

    /**
     * Find user profile by Keycloak user ID
     */
    Optional<UserProfile> findByKeycloakUserId(String keycloakUserId);

    Optional<UserProfile> findByEmail(String email);

    @Query(value = """
                SELECT *
                FROM user_profile
                WHERE id = ?1 OR keycloak_user_id = ?1
            """, nativeQuery = true)
    Optional<UserProfile> findByIdOrKeycloakUserId(String id);

    boolean existsByEmail(String email);
}
