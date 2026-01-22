package com.datn.datnbe.cms.repository;

import com.datn.datnbe.cms.entity.PostLinkedResource;
import com.datn.datnbe.cms.enums.LinkedResourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PostLinkedResourceRepository extends JpaRepository<PostLinkedResource, String> {

    List<PostLinkedResource> findByPostId(String postId);

    @Query("SELECT CASE WHEN COUNT(plr) > 0 THEN true ELSE false END " + "FROM PostLinkedResource plr "
            + "JOIN Post p ON plr.postId = p.id " + "WHERE p.classId = :classId "
            + "AND plr.resourceType = :resourceType " + "AND plr.resourceId = :resourceId")
    boolean isResourceLinkedInClass(@Param("classId") String classId,
            @Param("resourceType") LinkedResourceType resourceType,
            @Param("resourceId") String resourceId);

    @Modifying
    @Transactional
    void deleteByPostId(String postId);

    boolean existsByPostIdAndResourceTypeAndResourceId(String postId,
            LinkedResourceType resourceType,
            String resourceId);
}
