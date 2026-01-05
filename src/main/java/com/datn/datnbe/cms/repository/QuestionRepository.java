package com.datn.datnbe.cms.repository;

import com.datn.datnbe.cms.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, String> {

    Page<Question> findByOwnerId(String ownerId, Pageable pageable);

    Page<Question> findByOwnerIdIsNull(Pageable pageable);

    Page<Question> findByOwnerIdIsNullAndTitleContainingIgnoreCase(String search, Pageable pageable);

    Page<Question> findByOwnerIdAndTitleContainingIgnoreCase(String ownerId, String search, Pageable pageable);

    Page<Question> findByTitleContainingIgnoreCase(String search, Pageable pageable);

    boolean existsByIdAndOwnerId(String id, String ownerId);

    boolean existsByIdAndOwnerIdIsNull(String id);

    Optional<Question> findById(String id);
}
