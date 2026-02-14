package com.datn.datnbe.document.repository;

import com.datn.datnbe.document.entity.Context;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContextRepository extends JpaRepository<Context, String>, JpaSpecificationExecutor<Context> {

    List<Context> findByIdIn(List<String> ids);

    /**
     * Find contexts by grade and subject for random selection.
     * Used when generating assignments with context-based questions.
     *
     * @param grade   Grade level (e.g., "1", "2", "3", "4", "5", "K")
     * @param subject Subject code (e.g., "T", "TV", "TA")
     * @return List of contexts matching the criteria
     */
    List<Context> findByGradeAndSubject(String grade, String subject);
}
