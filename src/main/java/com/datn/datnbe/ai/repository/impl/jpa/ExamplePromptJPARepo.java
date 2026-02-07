package com.datn.datnbe.ai.repository.impl.jpa;

import com.datn.datnbe.ai.entity.ExamplePrompt;
import com.datn.datnbe.ai.enums.ExamplePromptType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamplePromptJPARepo extends JpaRepository<ExamplePrompt, String> {
    List<ExamplePrompt> findByType(ExamplePromptType type);
}
