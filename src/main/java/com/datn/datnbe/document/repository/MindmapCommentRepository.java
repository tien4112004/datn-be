package com.datn.datnbe.document.repository;

import com.datn.datnbe.document.entity.MindmapComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MindmapCommentRepository extends JpaRepository<MindmapComment, String> {

    /**
     * Find all non-deleted comments for a specific mindmap, ordered by creation time descending
     */
    List<MindmapComment> findByMindmapIdAndDeletedAtIsNullOrderByCreatedAtDesc(String mindmapId);

    /**
     * Find a non-deleted comment by ID
     */
    Optional<MindmapComment> findByIdAndDeletedAtIsNull(String id);

    /**
     * Count non-deleted comments for a mindmap
     */
    long countByMindmapIdAndDeletedAtIsNull(String mindmapId);
}
