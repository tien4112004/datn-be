package com.datn.datnbe.document.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.datn.datnbe.document.entity.Chapter;

public interface ChapterRepository extends JpaRepository<Chapter, String> {

    @Query("SELECT c FROM Chapter c ORDER BY c.sortOrder ASC")
    List<Chapter> findAll();

    @Query("SELECT c FROM Chapter c WHERE " +
            "(:grade IS NULL OR c.grade = :grade) AND " +
            "(:subject IS NULL OR c.subject = :subject) " +
            "ORDER BY c.sortOrder ASC")
    List<Chapter> findAllByGradeAndSubject(@Param("grade") String grade,
            @Param("subject") String subject);
}
