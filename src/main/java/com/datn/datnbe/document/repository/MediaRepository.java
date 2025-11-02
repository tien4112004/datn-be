package com.datn.datnbe.document.repository;

import com.datn.datnbe.document.entity.Media;
import com.datn.datnbe.sharedkernel.enums.MediaType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {

    Optional<Media> findByStorageKey(String storageKey);

    List<Media> findByMediaType(MediaType mediaType);

    Page<Media> findByMediaType(MediaType mediaType, Pageable pageable);

    @Query("SELECT m FROM Media m WHERE m.originalFilename LIKE %:filename%")
    List<Media> findByOriginalFilenameContaining(String filename);

    boolean existsByStorageKey(String storageKey);
}
