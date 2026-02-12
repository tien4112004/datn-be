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

    // Dashboard and Analytics Queries

    /**
     * Find all pending submissions for a teacher's classes
     */
    @Query(value = """
            SELECT s.* FROM submissions s
            JOIN posts p ON s.post_id = p.id
            JOIN classes c ON p.class_id = c.id
            WHERE c.owner_id = :teacherId
            AND s.status IN ('pending', 'in_progress')
            ORDER BY s.submitted_at ASC
            """, nativeQuery = true)
    List<Submission> findPendingSubmissionsByTeacher(String teacherId);

    /**
     * Find recent submissions for a teacher across all classes
     */
    @Query(value = """
            SELECT s.* FROM submissions s
            JOIN posts p ON s.post_id = p.id
            JOIN classes c ON p.class_id = c.id
            WHERE c.owner_id = :teacherId
            ORDER BY s.submitted_at DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Submission> findRecentSubmissionsByTeacher(String teacherId, int limit);

    /**
     * Find submissions by student across all classes
     */
    @Query("SELECT s FROM Submission s WHERE s.studentId = :studentId")
    List<Submission> findByStudentId(String studentId);

    /**
     * Find submissions by class
     */
    @Query(value = """
            SELECT s.* FROM submissions s
            JOIN posts p ON s.post_id = p.id
            WHERE p.class_id = :classId
            """, nativeQuery = true)
    List<Submission> findByClassId(String classId);

    /**
     * Count submissions by status for a specific class
     */
    @Query(value = """
            SELECT COUNT(*) FROM submissions s
            JOIN posts p ON s.post_id = p.id
            WHERE p.class_id = :classId AND s.status = :status
            """, nativeQuery = true)
    long countByClassIdAndStatus(String classId, String status);

    /**
     * Get average score for a student
     */
    @Query("SELECT AVG(s.score) FROM Submission s WHERE s.studentId = :studentId AND s.status = 'graded'")
    Double getAverageScoreByStudent(String studentId);

    /**
     * Get average score for a class
     */
    @Query(value = """
            SELECT AVG(s.score) FROM submissions s
            JOIN posts p ON s.post_id = p.id
            WHERE p.class_id = :classId AND s.status = 'graded'
            """, nativeQuery = true)
    Double getAverageScoreByClass(String classId);

    /**
     * Count total submissions by student
     */
    @Query("SELECT COUNT(s) FROM Submission s WHERE s.studentId = :studentId")
    long countByStudentId(String studentId);
}
