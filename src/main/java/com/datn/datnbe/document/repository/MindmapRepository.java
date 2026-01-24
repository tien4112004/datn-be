package com.datn.datnbe.document.repository;

import com.datn.datnbe.document.entity.Mindmap;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MindmapRepository extends JpaRepository<Mindmap, String> {
    @Query("SELECT m FROM Mindmap m WHERE LOWER(m.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    Page<Mindmap> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Mindmap> findAll(Pageable pageable);

    boolean existsByTitle(String title);

    @Query("SELECT m FROM Mindmap m WHERE m.id IN :ids")
    Page<Mindmap> findByIdIn(Iterable<String> ids, Pageable pageable);

    @Query("SELECT COUNT(m) FROM Mindmap m WHERE m.id IN :ids")
    long countByIdIn(@Param("ids") java.util.Collection<String> ids);
}
