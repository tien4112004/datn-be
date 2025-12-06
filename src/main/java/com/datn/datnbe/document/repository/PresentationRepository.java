package com.datn.datnbe.document.repository;

import com.datn.datnbe.document.entity.Presentation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

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
}
