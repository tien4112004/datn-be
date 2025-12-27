package com.datn.datnbe.document.repository;

import com.datn.datnbe.document.entity.SlideTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SlideTemplateRepository extends JpaRepository<SlideTemplate, String> {

    Page<SlideTemplate> findAllByIsEnabledTrue(Pageable pageable);

    Page<SlideTemplate> findByLayoutAndIsEnabledTrue(String layout, Pageable pageable);
}
