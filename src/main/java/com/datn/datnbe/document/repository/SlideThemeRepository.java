package com.datn.datnbe.document.repository;

import com.datn.datnbe.document.entity.SlideTheme;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SlideThemeRepository extends JpaRepository<SlideTheme, String> {

    Page<SlideTheme> findAllByIsEnabledTrue(Pageable pageable);
}
