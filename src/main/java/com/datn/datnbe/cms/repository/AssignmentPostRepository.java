package com.datn.datnbe.cms.repository;

import com.datn.datnbe.cms.entity.AssignmentPost;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AssignmentPostRepository extends JpaRepository<AssignmentPost, String> {

    /**
     * Find assignment from the assignment_post table (cloned assignments for posts)
     */
    @Query(value = """
            SELECT * FROM assignment_post
            WHERE id = :id
            """, nativeQuery = true)
    AssignmentPost findAssignmentById(String id);
}
