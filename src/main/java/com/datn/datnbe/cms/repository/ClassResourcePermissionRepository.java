package com.datn.datnbe.cms.repository;

import com.datn.datnbe.cms.entity.ClassResourcePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassResourcePermissionRepository extends JpaRepository<ClassResourcePermission, String> {

    Optional<ClassResourcePermission> findByClassIdAndResourceTypeAndResourceId(String classId,
            String resourceType,
            String resourceId);

    List<ClassResourcePermission> findByClassId(String classId);

    List<ClassResourcePermission> findByResourceTypeAndResourceId(String resourceType, String resourceId);

    boolean existsByClassIdAndResourceTypeAndResourceId(String classId, String resourceType, String resourceId);

    void deleteByClassIdAndResourceTypeAndResourceId(String classId, String resourceType, String resourceId);

    @Query("SELECT COUNT(crp) > 0 FROM ClassResourcePermission crp WHERE crp.classId = :classId "
            + "AND crp.resourceType = :resourceType AND crp.resourceId = :resourceId")
    boolean isResourceLinkedInClass(@Param("classId") String classId,
            @Param("resourceType") String resourceType,
            @Param("resourceId") String resourceId);
}
