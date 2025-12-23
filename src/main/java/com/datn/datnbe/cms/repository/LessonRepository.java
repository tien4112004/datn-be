package com.datn.datnbe.cms.repository;

import com.datn.datnbe.cms.entity.Lesson;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, String>, JpaSpecificationExecutor<Lesson> {

    @Query("SELECT l FROM Lesson l WHERE " + "(:classId IS NULL OR l.classId = :classId) AND "
            + "(:ownerId IS NULL OR l.ownerId = :ownerId) AND "
            + "(:search IS NULL OR LOWER(l.title) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR LOWER(l.subject) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))")
    Page<Lesson> findAllWithFilters(String classId, String ownerId, String search, Pageable pageable);
}
