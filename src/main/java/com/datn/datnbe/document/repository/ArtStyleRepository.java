package com.datn.datnbe.document.repository;

import com.datn.datnbe.document.entity.ArtStyle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArtStyleRepository extends JpaRepository<ArtStyle, String> {

    Page<ArtStyle> findAllByIsEnabledTrue(Pageable pageable);
}
