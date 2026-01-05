package com.datn.datnbe.student.exam.repository;

import com.datn.datnbe.student.exam.entity.Exam;
import com.datn.datnbe.student.exam.enums.ExamStatus;
import com.datn.datnbe.student.exam.enums.GradeLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExamRepository extends JpaRepository<Exam, UUID> {

    @Query("SELECT e FROM Exam e WHERE e.teacherId = :teacherId")
    Page<Exam> findByTeacherId(@Param("teacherId") UUID teacherId, Pageable pageable);

    @Query("SELECT e FROM Exam e WHERE e.teacherId = :teacherId AND e.status = :status")
    Page<Exam> findByTeacherIdAndStatus(@Param("teacherId") UUID teacherId,
            @Param("status") ExamStatus status,
            Pageable pageable);

    @Query("SELECT e FROM Exam e WHERE e.teacherId = :teacherId AND LOWER(e.topic) LIKE LOWER(CONCAT('%', :topic, '%'))")
    Page<Exam> findByTeacherIdAndTopicContaining(@Param("teacherId") UUID teacherId,
            @Param("topic") String topic,
            Pageable pageable);

    @Query("SELECT e FROM Exam e WHERE e.teacherId = :teacherId AND e.gradeLevel = :gradeLevel")
    Page<Exam> findByTeacherIdAndGradeLevel(@Param("teacherId") UUID teacherId,
            @Param("gradeLevel") GradeLevel gradeLevel,
            Pageable pageable);

    @Query("SELECT e FROM Exam e WHERE e.teacherId = :teacherId " + "AND (:status IS NULL OR e.status = :status) "
            + "AND (:topic IS NULL OR LOWER(e.topic) LIKE LOWER(CONCAT('%', :topic, '%'))) "
            + "AND (:gradeLevel IS NULL OR e.gradeLevel = :gradeLevel)")
    Page<Exam> findByTeacherIdWithFilters(@Param("teacherId") UUID teacherId,
            @Param("status") ExamStatus status,
            @Param("topic") String topic,
            @Param("gradeLevel") GradeLevel gradeLevel,
            Pageable pageable);

    @Query("SELECT e FROM Exam e WHERE e.examId = :examId AND e.teacherId = :teacherId")
    Optional<Exam> findByExamIdAndTeacherId(@Param("examId") UUID examId, @Param("teacherId") UUID teacherId);
}
