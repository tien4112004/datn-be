package com.datn.datnbe.cms.repository;

import com.datn.datnbe.cms.entity.QuestionBankItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<QuestionBankItem, String>, JpaSpecificationExecutor<QuestionBankItem> {

    Page<QuestionBankItem> findByOwnerId(String ownerId, Pageable pageable);

    Page<QuestionBankItem> findByOwnerIdIsNull(Pageable pageable);

    Page<QuestionBankItem> findByOwnerIdIsNullAndTitleContainingIgnoreCase(String search, Pageable pageable);

    Page<QuestionBankItem> findByOwnerIdAndTitleContainingIgnoreCase(String ownerId, String search, Pageable pageable);

    Page<QuestionBankItem> findByTitleContainingIgnoreCase(String search, Pageable pageable);

    boolean existsByIdAndOwnerId(String id, String ownerId);

    boolean existsByIdAndOwnerIdIsNull(String id);

    Optional<QuestionBankItem> findById(String id);
}
