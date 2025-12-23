package com.datn.datnbe.cms.repository;

import com.datn.datnbe.cms.entity.LessonResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonResourceRepository extends JpaRepository<LessonResource, String> {

    @Query("SELECT r FROM LessonResource r WHERE (:lessonId IS NULL OR r.lessonId = :lessonId)")
    List<LessonResource> findByLessonId(String lessonId);

    @Query("SELECT r FROM LessonResource r WHERE (:lessonId IS NULL OR r.lessonId = :lessonId)")
    Page<LessonResource> findByLessonIdPageable(String lessonId, Pageable pageable);
}
