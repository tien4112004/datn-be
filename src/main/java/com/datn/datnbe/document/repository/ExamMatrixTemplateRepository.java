package com.datn.datnbe.document.repository;

import com.datn.datnbe.document.entity.ExamMatrix;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamMatrixTemplateRepository extends JpaRepository<ExamMatrix, String> {

    /**
     * Find all matrices created by a specific owner.
     */
    @Query("SELECT em FROM ExamMatrix em WHERE em.ownerId = :ownerId ORDER BY em.createdAt DESC")
    List<ExamMatrix> findByOwnerId(@Param("ownerId") String ownerId);

    /**
     * Find matrix by ID and owner (for access control).
     */
    @Query("SELECT em FROM ExamMatrix em WHERE em.id = :id AND em.ownerId = :ownerId")
    Optional<ExamMatrix> findByIdAndOwnerId(@Param("id") String id, @Param("ownerId") String ownerId);

    /**
     * Check if a matrix exists for a specific assignment/id.
     */
    boolean existsById(String id);

    /**
     * Find matrices by owner and subject.
     */
    @Query("SELECT em FROM ExamMatrix em WHERE em.ownerId = :ownerId AND em.subject = :subject ORDER BY em.createdAt DESC")
    List<ExamMatrix> findByOwnerIdAndSubject(@Param("ownerId") String ownerId, @Param("subject") String subject);

    /**
     * Find matrices by owner and grade.
     */
    @Query("SELECT em FROM ExamMatrix em WHERE em.ownerId = :ownerId AND em.grade = :grade ORDER BY em.createdAt DESC")
    List<ExamMatrix> findByOwnerIdAndGrade(@Param("ownerId") String ownerId, @Param("grade") String grade);

    /**
     * Find matrices by owner, subject, and grade.
     */
    @Query("SELECT em FROM ExamMatrix em WHERE em.ownerId = :ownerId AND em.subject = :subject AND em.grade = :grade ORDER BY em.createdAt DESC")
    List<ExamMatrix> findByOwnerIdAndSubjectAndGrade(@Param("ownerId") String ownerId,
            @Param("subject") String subject,
            @Param("grade") String grade);

    /**
     * Search matrices by name (case-insensitive contains).
     */
    @Query("SELECT em FROM ExamMatrix em WHERE em.ownerId = :ownerId AND LOWER(em.name) LIKE LOWER(CONCAT('%', :name, '%')) ORDER BY em.createdAt DESC")
    List<ExamMatrix> searchByOwnerIdAndName(@Param("ownerId") String ownerId, @Param("name") String name);
}
