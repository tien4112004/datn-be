package com.datn.datnbe.document.repository;

import com.datn.datnbe.document.entity.AssignmentQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentQuestionRepository
        extends
            JpaRepository<AssignmentQuestion, String>,
            JpaSpecificationExecutor<AssignmentQuestion> {
    List<AssignmentQuestion> findByAssignmentIdOrderByOrderAsc(String assignmentId);

    Optional<AssignmentQuestion> findByAssignmentIdAndQuestionId(String assignmentId, String questionId);
}
