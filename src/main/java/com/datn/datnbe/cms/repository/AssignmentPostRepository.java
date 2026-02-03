package com.datn.datnbe.cms.repository;

import com.datn.datnbe.cms.entity.AssignmentPost;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AssignmentPostRepository extends JpaRepository<AssignmentPost, String> {

    @Query(value = """
            SELECT * FROM Assigments
            WHERE id = :id
            """, nativeQuery = true)
    public AssignmentPost findAssignmentById(String id);
}
