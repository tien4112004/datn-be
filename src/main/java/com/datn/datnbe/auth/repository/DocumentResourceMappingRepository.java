package com.datn.datnbe.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.datn.datnbe.auth.entity.DocumentResourceMapping;

@Repository
public interface DocumentResourceMappingRepository extends JpaRepository<DocumentResourceMapping, String> {

    Optional<DocumentResourceMapping> findByDocumentId(String documentId);

    boolean existsByDocumentId(String documentId);
}
