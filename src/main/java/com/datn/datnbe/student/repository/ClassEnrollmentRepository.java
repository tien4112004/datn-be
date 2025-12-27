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

    void deleteByClassIdAndStudentId(String classId, String studentId);
}
