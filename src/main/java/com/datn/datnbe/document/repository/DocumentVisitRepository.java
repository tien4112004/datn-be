package com.datn.datnbe.document.repository;

import com.datn.datnbe.document.dto.response.DocumentMinimalResponseDto;
import com.datn.datnbe.document.entity.DocumentVisit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentVisitRepository extends JpaRepository<DocumentVisit, Integer> {

    /**
     * Get recent documents visited by user, sorted by last visit time
     */
    @Query(value = """
            SELECT dv FROM DocumentVisit dv
            WHERE dv.userId = :userId
            ORDER BY dv.lastVisited DESC
            """)
    Page<DocumentVisit> findRecentDocumentsByUser(String userId, Pageable pageable);

    /**
     * Find document visit record for a specific user and document
     */
    Optional<DocumentVisit> findByUserIdAndDocumentId(String userId, String documentId);

    /**
     * Delete all visits for a document (when document is deleted)
     */
    void deleteByDocumentId(String documentId);

    @Query(value = """
            SELECT drm.document_id as id, drm.resource_type as type, rsr.display_name as title
            FROM document_resource_mappings drm JOIN resource_service_resource rsr on drm.keycloak_resource_id = rsr.id
            WHERE drm.document_id = :documentId
            """, nativeQuery = true)
    Optional<DocumentMinimalResponseDto> findMinimalDocumentInfoByDocumentId(String documentId);
}
