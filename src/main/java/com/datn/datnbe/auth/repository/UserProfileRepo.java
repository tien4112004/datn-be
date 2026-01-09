package com.datn.datnbe.auth.repository;

import com.datn.datnbe.auth.entity.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    @Query(value = """
                SELECT *
                FROM user_profile
                WHERE id IN (?1) OR keycloak_user_id IN (?1)
            """, nativeQuery = true)
    List<UserProfile> findAllByIdOrKeycloakUserIdIn(List<String> ids);

    boolean existsByEmail(String email);

    /**
     * Find user profiles by name or email with pagination
     * Searches in first name, last name, and email fields
     */
    @Query("""
            SELECT u FROM user_profile u
            WHERE (:searchTerm IS NULL OR :searchTerm = '' OR
                   LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR
                   LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR
                   LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
            """)
    Page<UserProfile> findBySearchTerm(@Param("searchTerm") String searchTerm, Pageable pageable);
}
