package com.datn.datnbe.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.datn.datnbe.auth.entity.FileResource;

/**
 * Repository for FileResource entity.
 * Provides data access methods for file management.
 */
@Repository
public interface FileResourceRepository extends JpaRepository<FileResource, String> {

    /**
     * Find a file by its Keycloak resource ID
     * @param keycloakResourceId The Keycloak resource ID
     * @return Optional containing the file if found
     */
    Optional<FileResource> findByKeycloakResourceId(String keycloakResourceId);

    /**
     * Find a file by ID and owner ID
     * @param id The file ID
     * @param ownerId The owner's user ID
     * @return Optional containing the file if found
     */
    Optional<FileResource> findByIdAndOwnerId(String id, String ownerId);
}
