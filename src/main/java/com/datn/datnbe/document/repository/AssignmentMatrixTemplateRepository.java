package com.datn.datnbe.document.repository;

import com.datn.datnbe.document.entity.AssignmentMatrixEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentMatrixTemplateRepository extends JpaRepository<AssignmentMatrixEntity, String> {

    /**
     * Find all matrices created by a specific owner.
     */
    @Query("SELECT am FROM AssignmentMatrixEntity am WHERE am.ownerId = :ownerId ORDER BY am.createdAt DESC")
    List<AssignmentMatrixEntity> findByOwnerId(@Param("ownerId") String ownerId);

    /**
     * Find matrix by ID and owner (for access control).
     */
    @Query("SELECT am FROM AssignmentMatrixEntity am WHERE am.id = :id AND am.ownerId = :ownerId")
    Optional<AssignmentMatrixEntity> findByIdAndOwnerId(@Param("id") String id, @Param("ownerId") String ownerId);

    /**
     * Check if a matrix exists for a specific assignment/id.
     */
    boolean existsById(String id);

    /**
     * Find matrices by owner and subject.
     */
    @Query("SELECT am FROM AssignmentMatrixEntity am WHERE am.ownerId = :ownerId AND am.subject = :subject ORDER BY am.createdAt DESC")
    List<AssignmentMatrixEntity> findByOwnerIdAndSubject(@Param("ownerId") String ownerId,
            @Param("subject") String subject);

    /**
     * Find matrices by owner and grade.
     */
    @Query("SELECT am FROM AssignmentMatrixEntity am WHERE am.ownerId = :ownerId AND am.grade = :grade ORDER BY am.createdAt DESC")
    List<AssignmentMatrixEntity> findByOwnerIdAndGrade(@Param("ownerId") String ownerId, @Param("grade") String grade);

    /**
     * Find matrices by owner, subject, and grade.
     */
    @Query("SELECT am FROM AssignmentMatrixEntity am WHERE am.ownerId = :ownerId AND am.subject = :subject AND am.grade = :grade ORDER BY am.createdAt DESC")
    List<AssignmentMatrixEntity> findByOwnerIdAndSubjectAndGrade(@Param("ownerId") String ownerId,
            @Param("subject") String subject,
            @Param("grade") String grade);

    /**
     * Search matrices by name (case-insensitive contains).
     */
    @Query("SELECT am FROM AssignmentMatrixEntity am WHERE am.ownerId = :ownerId AND LOWER(am.name) LIKE LOWER(CONCAT('%', :name, '%')) ORDER BY am.createdAt DESC")
    List<AssignmentMatrixEntity> searchByOwnerIdAndName(@Param("ownerId") String ownerId, @Param("name") String name);

    // Public template queries (ownerId IS NULL)

    /**
     * Find all public matrices (ownerId IS NULL).
     */
    @Query("SELECT am FROM AssignmentMatrixEntity am WHERE am.ownerId IS NULL ORDER BY am.createdAt DESC")
    List<AssignmentMatrixEntity> findByOwnerIdIsNull();

    /**
     * Find public matrices by subject.
     */
    @Query("SELECT am FROM AssignmentMatrixEntity am WHERE am.ownerId IS NULL AND am.subject = :subject ORDER BY am.createdAt DESC")
    List<AssignmentMatrixEntity> findByOwnerIdIsNullAndSubject(@Param("subject") String subject);

    /**
     * Find public matrices by grade.
     */
    @Query("SELECT am FROM AssignmentMatrixEntity am WHERE am.ownerId IS NULL AND am.grade = :grade ORDER BY am.createdAt DESC")
    List<AssignmentMatrixEntity> findByOwnerIdIsNullAndGrade(@Param("grade") String grade);

    /**
     * Find public matrices by subject and grade.
     */
    @Query("SELECT am FROM AssignmentMatrixEntity am WHERE am.ownerId IS NULL AND am.subject = :subject AND am.grade = :grade ORDER BY am.createdAt DESC")
    List<AssignmentMatrixEntity> findByOwnerIdIsNullAndSubjectAndGrade(@Param("subject") String subject,
            @Param("grade") String grade);

    /**
     * Search public matrices by name (case-insensitive contains).
     */
    @Query("SELECT am FROM AssignmentMatrixEntity am WHERE am.ownerId IS NULL AND LOWER(am.name) LIKE LOWER(CONCAT('%', :name, '%')) ORDER BY am.createdAt DESC")
    List<AssignmentMatrixEntity> searchByOwnerIdIsNullAndName(@Param("name") String name);
}
