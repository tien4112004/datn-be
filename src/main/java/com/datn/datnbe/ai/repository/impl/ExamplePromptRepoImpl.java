package com.datn.datnbe.ai.repository.impl;

import com.datn.datnbe.ai.entity.ExamplePrompt;
import com.datn.datnbe.ai.enums.ExamplePromptType;
import com.datn.datnbe.ai.repository.impl.jpa.ExamplePromptJPARepo;
import com.datn.datnbe.ai.repository.interfaces.ExamplePromptRepo;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ExamplePromptRepoImpl implements ExamplePromptRepo {

    ExamplePromptJPARepo examplePromptJPARepo;

    @Override
    public ExamplePrompt save(ExamplePrompt examplePrompt) {
        return examplePromptJPARepo.save(examplePrompt);
    }

    @Override
    public Optional<ExamplePrompt> findById(String id) {
        return examplePromptJPARepo.findById(id);
    }

    @Override
    public List<ExamplePrompt> findAll() {
        return examplePromptJPARepo.findAll();
    }

    @Override
    public List<ExamplePrompt> findByType(ExamplePromptType type) {
        return examplePromptJPARepo.findByType(type);
    }

    @Override
    public void deleteById(String id) {
        examplePromptJPARepo.deleteById(id);
    }
}
