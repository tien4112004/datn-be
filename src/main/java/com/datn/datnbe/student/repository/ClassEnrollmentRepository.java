package com.datn.datnbe.student.repository;

import com.datn.datnbe.student.entity.ClassEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassEnrollmentRepository extends JpaRepository<ClassEnrollment, String> {

    List<ClassEnrollment> findByClassId(String classId);

    Optional<ClassEnrollment> findByClassIdAndStudentId(String classId, String studentId);

    boolean existsByClassIdAndStudentId(String classId, String studentId);

    @org.springframework.data.jpa.repository.Query(value = """
            SELECT ce.student_id FROM class_enrollments ce
            WHERE ce.class_id = :classId AND ce.student_id IN :studentIds
            """, nativeQuery = true)
    java.util.Set<String> findAlreadyEnrolledStudentIds(
            @org.springframework.data.repository.query.Param("classId") String classId,
            @org.springframework.data.repository.query.Param("studentIds") java.util.Collection<String> studentIds);

    void deleteByClassIdAndStudentId(String classId, String studentId);
}
