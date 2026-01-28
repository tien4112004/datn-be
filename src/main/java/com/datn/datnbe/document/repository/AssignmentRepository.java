package com.datn.datnbe.document.repository;

import com.datn.datnbe.document.dto.AssignmentSummaryProjection;
import com.datn.datnbe.document.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Collection;
import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, String>, JpaSpecificationExecutor<Assignment> {
    Page<Assignment> findByIdIn(List<String> ids, Pageable pageable);

    @Query("SELECT COUNT(a) FROM Assignment a WHERE a.id IN :ids")
    long countByIdIn(@Param("ids") Collection<String> ids);

    /**
     * Fetch only id and title for given IDs.
     * Assignments don't have thumbnails.
     */
    @Query("SELECT a.id AS id, a.title AS title FROM Assignment a WHERE a.id IN :ids")
    List<AssignmentSummaryProjection> findSummariesByIds(@Param("ids") Collection<String> ids);
}
