package com.datn.datnbe.cms.repository;

import com.datn.datnbe.cms.entity.ClassEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassRepository extends JpaRepository<ClassEntity, String>, JpaSpecificationExecutor<ClassEntity> {
    @Query(value = "SELECT c.* FROM classes c " +
            "LEFT JOIN class_enrollments ce on ce.class_id = c.id " +
            "LEFT JOIN students s on s.id = ce.student_id " +
			"LEFT JOIN user_profile up on up.id = s.user_id " +
            "WHERE (:search IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%'))) " +
            "AND (:ownerId IS NULL OR c.owner_id = :ownerId OR :ownerId = up.keycloak_user_id) " +
            "AND (:isActive IS NULL OR c.is_active = :isActive)", nativeQuery = true)
    Page<ClassEntity> findAllWithFilters(@Param("search") String search,
            @Param("ownerId") String ownerId,
            @Param("isActive") Boolean isActive,
            Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END "
            + " FROM ClassEntity c WHERE c.id = :classId AND c.ownerId = :ownerId")
    boolean isTheOwnerOfClass(String classId, String ownerId);
}
