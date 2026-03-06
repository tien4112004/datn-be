package com.datn.datnbe.document.repository;

import com.datn.datnbe.document.dto.response.DocumentMinimalResponseDto;
import com.datn.datnbe.document.entity.DocumentVisit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<DocumentVisit, Integer> {

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

    @Query(value = """
            SELECT t.data FROM (
                SELECT
                    p.created_at as created_at,
                    jsonb_build_object(
                        'id', p.id,
                        'title', p.title,
                        'thumbnail', p.thumbnail,
                        'createdAt', p.created_at,
                        'updatedAt', p.updated_at,
                        'type', 'presentation'
                    ) AS data
                FROM presentations p
                WHERE p.title LIKE CONCAT('%', :keyword, '%') AND (:chapter IS NULL OR p.chapter = :chapter) AND (:subject IS NULL OR p.subject = :subject) AND (:grade IS NULL OR p.grade = :grade) AND p.id IN (:presentationIds)
                AND p.deleted_at IS NULL
                UNION ALL

                SELECT
                    m.created_at,
                    jsonb_build_object(
                        'id', m.id,
                        'title', m.title,
                        'description', m.description,
                        'thumbnail', m.thumbnail,
                        'createdAt', m.created_at,
                        'updatedAt', m.updated_at,
                        'type', 'mindmap'
                    ) AS data
                FROM mindmaps m
                WHERE m.title LIKE CONCAT('%', :keyword, '%') AND (:chapter IS NULL OR m.chapter = :chapter) AND (:subject IS NULL OR m.subject = :subject) AND (:grade IS NULL OR m.grade = :grade) AND m.id IN (:mindmapIds)


                UNION ALL

                SELECT
                    a.created_at,
                    to_jsonb(a) || jsonb_build_object('type','assignment')
                FROM assignments a
                WHERE a.title LIKE CONCAT('%', :keyword, '%') AND (:chapter IS NULL OR a.chapter = :chapter) AND (:subject IS NULL OR a.subject = :subject) AND (:grade IS NULL OR a.grade = :grade) AND a.id IN (:assignmentIds)
            ) t
            """, countQuery = """
            SELECT
                (
                    SELECT COUNT(*)
                    FROM presentations p
                    WHERE p.title LIKE CONCAT('%', :keyword, '%')
                    AND (:chapter IS NULL OR p.chapter = :chapter)
                    AND (:subject IS NULL OR p.subject = :subject)
                    AND (:grade IS NULL OR p.grade = :grade)
                    AND p.id IN (:presentationIds)
                    AND p.deleted_at IS NULL
                )
                +
                (
                    SELECT COUNT(*)
                    FROM mindmaps m
                    WHERE m.title LIKE CONCAT('%', :keyword, '%')
                    AND (:chapter IS NULL OR m.chapter = :chapter)
                    AND (:subject IS NULL OR m.subject = :subject)
                    AND (:grade IS NULL OR m.grade = :grade)
                    AND m.id IN (:mindmapIds)
                )
                +
                (
                    SELECT COUNT(*)
                    FROM assignments a
                    WHERE a.title LIKE CONCAT('%', :keyword, '%')
                    AND (:chapter IS NULL OR a.chapter = :chapter)
                    AND (:subject IS NULL OR a.subject = :subject)
                    AND (:grade IS NULL OR a.grade = :grade)
                    AND a.id IN (:assignmentIds)
                )
            AS total_count
            """, nativeQuery = true)
    Page<String> getAllDocuments(Pageable pageable,
            @Param("keyword") String keyword,
            @Param("chapter") String chapter,
            @Param("subject") String subject,
            @Param("grade") String grade,
            @Param("presentationIds") List<String> presentationIds,
            @Param("mindmapIds") List<String> mindmapIds,
            @Param("assignmentIds") List<String> assignmentIds);
}
