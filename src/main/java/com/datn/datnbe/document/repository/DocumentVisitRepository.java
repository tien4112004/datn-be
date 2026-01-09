package com.datn.datnbe.document.repository;

import com.datn.datnbe.document.entity.DocumentVisit;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
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
    List<DocumentVisit> findRecentDocumentsByUser(String userId, Pageable pageable);

    /**
     * Find document visit record for a specific user and document
     */
    Optional<DocumentVisit> findByUserIdAndDocumentId(String userId, String documentId);

    /**
     * Delete all visits for a document (when document is deleted)
     */
    void deleteByDocumentId(String documentId);
}
