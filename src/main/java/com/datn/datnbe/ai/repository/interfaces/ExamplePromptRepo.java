package com.datn.datnbe.ai.repository.interfaces;

import com.datn.datnbe.ai.entity.ExamplePrompt;
import com.datn.datnbe.ai.enums.ExamplePromptType;

import java.util.List;
import java.util.Optional;

public interface ExamplePromptRepo {
    ExamplePrompt save(ExamplePrompt examplePrompt);

    Optional<ExamplePrompt> findById(String id);

    List<ExamplePrompt> findAll();

    List<ExamplePrompt> findByType(ExamplePromptType type);

    void deleteById(String id);
}
