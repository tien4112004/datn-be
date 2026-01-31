package com.datn.datnbe.cms.repository;

import com.datn.datnbe.cms.entity.Post;
import com.datn.datnbe.document.dto.response.AssignmentResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PostRepository extends JpaRepository<Post, String> {

    @Query("SELECT p FROM Post p WHERE (:classId IS NULL OR p.classId = :classId) AND "
            + "(:type IS NULL OR p.type = :type) AND "
            + "(:search IS NULL OR LOWER(p.content) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))) "
            + "ORDER BY p.isPinned DESC, p.createdAt DESC")
    Page<Post> findAllWithFilters(String classId, String type, String search, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE Post p SET p.commentCount = p.commentCount + :delta WHERE p.id = :postId")
    void updateCommentCount(@Param("postId") String postId, @Param("delta") int delta);

    @Query(value = """
            SELECT a.*
            FROM assignment_post a JOIN posts p ON a.id = p.assignment_id
            WHERE p.id = :postId
            """, nativeQuery = true)
    AssignmentResponse getAssignmentByPostId(String postId);
}
