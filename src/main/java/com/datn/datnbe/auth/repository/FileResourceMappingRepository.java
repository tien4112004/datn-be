package com.datn.datnbe.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.datn.datnbe.auth.entity.FileResourceMapping;

/**
 * Repository for file-to-Keycloak resource mappings.
 * Simple lookup table to find Keycloak resource ID from your file ID.
 */
@Repository
public interface FileResourceMappingRepository extends JpaRepository<FileResourceMapping, String> {

    /**
     * Find mapping by your file ID
     * @param fileId Your file ID
     * @return Mapping containing the Keycloak resource ID
     */
    Optional<FileResourceMapping> findByFileId(String fileId);

    /**
     * Find mapping by Keycloak resource ID
     * @param keycloakResourceId Keycloak resource ID
     * @return Mapping containing your file ID
     */
    Optional<FileResourceMapping> findByKeycloakResourceId(String keycloakResourceId);

    /**
     * Check if mapping exists for a file
     * @param fileId Your file ID
     * @return true if mapping exists
     */
    boolean existsByFileId(String fileId);

    /**
     * Delete mapping by file ID
     * @param fileId Your file ID
     */
    void deleteByFileId(String fileId);
}
