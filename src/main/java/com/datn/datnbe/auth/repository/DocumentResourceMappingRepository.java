package com.datn.datnbe.auth.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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
            WHERE drm.resource_type = :resourceType AND rsr.owner = :ownerId
            """, nativeQuery = true)
    List<ResourceResponse> findAllResourcesByOwner(String ownerId);
}
