package com.datn.datnbe.document.repository;

import com.datn.datnbe.document.entity.Context;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContextRepository extends JpaRepository<Context, String>, JpaSpecificationExecutor<Context> {

    List<Context> findByIdIn(List<String> ids);
}
