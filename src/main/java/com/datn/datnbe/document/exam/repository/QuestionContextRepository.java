package com.datn.datnbe.document.exam.repository;

import com.datn.datnbe.document.exam.entity.QuestionContext;
import com.datn.datnbe.document.exam.enums.ContextType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuestionContextRepository extends JpaRepository<QuestionContext, UUID> {

    @Query("SELECT qc FROM QuestionContext qc WHERE qc.ownerId = :ownerId")
    Page<QuestionContext> findByOwnerId(@Param("ownerId") UUID ownerId, Pageable pageable);

    @Query("SELECT qc FROM QuestionContext qc WHERE qc.ownerId = :ownerId AND qc.contextType = :contextType")
    Page<QuestionContext> findByOwnerIdAndContextType(@Param("ownerId") UUID ownerId,
            @Param("contextType") ContextType contextType,
            Pageable pageable);

    @Query("SELECT qc FROM QuestionContext qc WHERE qc.contextId = :contextId AND qc.ownerId = :ownerId")
    Optional<QuestionContext> findByContextIdAndOwnerId(@Param("contextId") UUID contextId,
            @Param("ownerId") UUID ownerId);
}
