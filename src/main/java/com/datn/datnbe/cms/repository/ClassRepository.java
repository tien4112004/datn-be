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
    @Query("SELECT c FROM ClassEntity c WHERE "
            + "(:search IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))) AND "
            + "(:ownerId IS NULL OR c.ownerId = :ownerId) AND " + "(:isActive IS NULL OR c.isActive = :isActive)")
    Page<ClassEntity> findAllWithFilters(@Param("search") String search,
            @Param("ownerId") String ownerId,
            @Param("isActive") Boolean isActive,
            Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END "
            + " FROM ClassEntity c WHERE c.id = :classId AND c.ownerId = :ownerId")
    boolean isTheOwnerOfClass(String classId, String ownerId);
}
