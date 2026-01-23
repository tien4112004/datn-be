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

    @Query("SELECT s FROM Submission s WHERE (:lessonId IS NULL OR s.lessonId = :lessonId)")
    List<Submission> findByLessonId(String lessonId);

    @Query("SELECT s FROM Submission s WHERE (:lessonId IS NULL OR s.lessonId = :lessonId)")
    Page<Submission> findByLessonIdPageable(String lessonId, Pageable pageable);

    
    @Query("SELECT s FROM Submission s WHERE (:postId IS NULL OR s.postId = :postId)")
    List<Submission> findByPostId(String postId);

    @Query("SELECT s FROM Submission s WHERE (:postId IS NULL OR s.postId = :postId)")
    Page<Submission> findByPostIdPageable(String postId, Pageable pageable);
}
