package com.datn.datnbe.auth.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.datn.datnbe.auth.dto.SharedResourceProjection;
import com.datn.datnbe.auth.dto.response.ResourceResponse;
import com.datn.datnbe.auth.entity.DocumentResourceMapping;

@Repository
public interface DocumentResourceMappingRepository extends JpaRepository<DocumentResourceMapping, String> {

    Optional<DocumentResourceMapping> findByDocumentId(String documentId);

    boolean existsByDocumentId(String documentId);

    @Query(value = """
            SELECT drm.document_id
            FROM document_resource_mappings drm
            JOIN resource_server_resource rsr ON drm.keycloak_resource_id = rsr.id
            WHERE drm.resource_type = :resourceType AND rsr.owner = :ownerId
            """, nativeQuery = true)
    List<String> findResourcesByTypeOfOwner(String resourceType, String ownerId);

    @Query(value = """
            SELECT drm.document_id as id, drm.resource_type AS type, drm.title AS title, drm.thumbnail AS thumbnail
            FROM document_resource_mappings drm
            JOIN resource_server_resource rsr ON drm.keycloak_resource_id = rsr.id
            WHERE rsr.owner = :ownerId
            """, nativeQuery = true)
    List<ResourceResponse> findAllResourcesByOwner(String ownerId);

    /**
     * Find all resources that could potentially be shared (have reader or commenter groups).
     * Only returns mindmap and presentation resource types.
     * Excludes resources owned by the user.
     * The actual filtering by group membership is done in the service layer via Keycloak API.
     * Title and thumbnail are fetched from source tables to avoid stale data.
     */
    @Query(value = """
            SELECT drm.document_id AS id,
                   drm.resource_type AS type,
                   drm.owner_id AS ownerId,
                   drm.readers_group_id AS readersGroupId,
                   drm.commenters_group_id AS commentersGroupId
            FROM document_resource_mappings drm
            WHERE drm.owner_id != :userId
              AND drm.resource_type IN ('mindmap', 'presentation')
              AND (drm.readers_group_id IS NOT NULL OR drm.commenters_group_id IS NOT NULL)
            """, nativeQuery = true)
    List<SharedResourceProjection> findPotentiallySharedResources(@Param("userId") String userId);
}
