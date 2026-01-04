package com.datn.datnbe.document.repository;

import com.datn.datnbe.document.entity.PresentationComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PresentationCommentRepository extends JpaRepository<PresentationComment, String> {

    /**
     * Find all non-deleted comments for a specific presentation, ordered by creation time descending
     */
    List<PresentationComment> findByPresentationIdAndDeletedAtIsNullOrderByCreatedAtDesc(String presentationId);

    /**
     * Find a non-deleted comment by ID
     */
    Optional<PresentationComment> findByIdAndDeletedAtIsNull(String id);

    /**
     * Count non-deleted comments for a presentation
     */
    long countByPresentationIdAndDeletedAtIsNull(String presentationId);
}
