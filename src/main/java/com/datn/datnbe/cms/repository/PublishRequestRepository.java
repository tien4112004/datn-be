package com.datn.datnbe.cms.repository;

import com.datn.datnbe.cms.entity.PublishRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PublishRequestRepository extends JpaRepository<PublishRequest, String> {

    Optional<PublishRequest> findByQuestionIdAndRequesterId(String questionId, String requesterId);

    Optional<PublishRequest> findByQuestionIdAndRequesterIdAndIsDeletedFalse(String questionId, String requesterId);

    Page<PublishRequest> findByStatusAndIsDeletedFalse(PublishRequest.PublishRequestStatus status, Pageable pageable);

    Page<PublishRequest> findByQuestionIdAndIsDeletedFalse(String questionId, Pageable pageable);

    boolean existsByQuestionIdAndRequesterIdAndIsDeletedFalse(String questionId, String requesterId);

    void deleteByQuestionId(String questionId);
}
