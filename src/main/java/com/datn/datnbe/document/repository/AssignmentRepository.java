package com.datn.datnbe.document.repository;

import com.datn.datnbe.document.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, String>, JpaSpecificationExecutor<Assignment> {
    Page<Assignment> findByIdIn(List<String> ids, Pageable pageable);
}
