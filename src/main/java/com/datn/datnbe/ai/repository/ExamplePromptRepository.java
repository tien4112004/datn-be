package com.datn.datnbe.ai.repository;

import com.datn.datnbe.ai.entity.ExamplePrompt;
import com.datn.datnbe.ai.enums.ExamplePromptType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamplePromptRepository extends JpaRepository<ExamplePrompt, String> {
    List<ExamplePrompt> findByType(ExamplePromptType type);

    List<ExamplePrompt> findByTypeAndLanguage(ExamplePromptType type, String language);

    @Query(value = "SELECT * FROM example_prompts WHERE type = CAST(:type AS VARCHAR) AND language = :language ORDER BY RANDOM() LIMIT :count", nativeQuery = true)
    List<ExamplePrompt> findRandomByTypeAndLanguage(String type, String language, int count);
}
