package com.datn.datnbe.ai.repository;

import com.datn.datnbe.ai.entity.ExamplePrompt;
import com.datn.datnbe.ai.enums.ExamplePromptType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamplePromptRepository extends JpaRepository<ExamplePrompt, String> {
    List<ExamplePrompt> findByType(ExamplePromptType type);
}
