package com.datn.datnbe.cms.repository;

import com.datn.datnbe.cms.entity.Submission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, String> {
    @Query("SELECT s FROM Submission s WHERE (:postId IS NULL OR s.postId = :postId)")
    List<Submission> findByPostId(String postId);

    @Query("SELECT s FROM Submission s WHERE (:postId IS NULL OR s.postId = :postId)")
    Page<Submission> findByPostIdPageable(String postId, Pageable pageable);

    List<Submission> findByAssignmentId(String assignmentId);

    Page<Submission> findByAssignmentId(String assignmentId, Pageable pageable);

    List<Submission> findByAssignmentIdAndStudentId(String assignmentId, String studentId);

    @Query("SELECT s FROM Submission s WHERE s.postId = :postId "
            + "AND (:studentId IS NULL OR s.studentId = :studentId) " + "AND (:status IS NULL OR s.status = :status)")
    List<Submission> findByPostIdWithFilters(String postId, String studentId, String status);

    @Query("SELECT COUNT(s) FROM Submission s WHERE s.assignmentId = :assignmentId AND s.status = :status")
    long countByAssignmentIdAndStatus(String assignmentId, String status);

    @Query("SELECT AVG(s.score) FROM Submission s WHERE s.assignmentId = :assignmentId AND s.status = 'graded'")
    Double getAverageScore(String assignmentId);

    @Query("SELECT MAX(s.score) FROM Submission s WHERE s.assignmentId = :assignmentId AND s.status = 'graded'")
    Double getHighestScore(String assignmentId);

    @Query("SELECT MIN(s.score) FROM Submission s WHERE s.assignmentId = :assignmentId AND s.status = 'graded'")
    Double getLowestScore(String assignmentId);

    long countByAssignmentIdAndStudentId(String assignmentId, String studentId);
}
