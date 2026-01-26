package com.datn.datnbe.document.repository;

import com.datn.datnbe.document.dto.ResourceSummaryProjection;
import com.datn.datnbe.document.entity.Presentation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PresentationRepository extends JpaRepository<Presentation, String> {

    @Query("SELECT p FROM Presentation p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%')) AND p.deletedAt IS NULL")
    Page<Presentation> findByTitleContainingIgnoreCase(@Param("title") String title, Pageable pageable);

    @Query("SELECT p FROM Presentation p WHERE p.deletedAt IS NULL")
    Page<Presentation> findAllActive(Pageable pageable);

    @Query("SELECT p FROM Presentation p WHERE p.id = :id AND p.deletedAt IS NULL")
    Optional<Presentation> findByIdActive(@Param("id") String id);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Presentation p WHERE p.title = :title AND p.deletedAt IS NULL")
    boolean existsByTitle(@Param("title") String title);

    @Query("SELECT p FROM Presentation p WHERE p.id IN :ids AND LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%')) AND p.deletedAt IS NULL")
    Page<Presentation> findByIdInWithOptionalTitle(@Param("ids") Iterable<String> ids,
            @Param("title") String title,
            Pageable pageable);

    @Query("SELECT COUNT(p) FROM Presentation p WHERE p.id IN :ids AND p.deletedAt IS NULL")
    long countByIdInAndDeletedAtIsNull(@Param("ids") java.util.Collection<String> ids);

    /**
     * Fetch only id, title, and thumbnail for given IDs.
     * Avoids loading heavy JSON columns like slides.
     */
    @Query("SELECT p.id AS id, p.title AS title, p.thumbnail AS thumbnail FROM Presentation p WHERE p.id IN :ids AND p.deletedAt IS NULL")
    List<ResourceSummaryProjection> findSummariesByIds(@Param("ids") Collection<String> ids);
}
