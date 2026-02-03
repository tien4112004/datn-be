package com.datn.datnbe.document.repository;

import com.datn.datnbe.document.entity.MatrixTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatrixTemplateRepository extends JpaRepository<MatrixTemplate, String> {
}
