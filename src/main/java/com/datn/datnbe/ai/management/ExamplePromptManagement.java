package com.datn.datnbe.ai.management;

import com.datn.datnbe.ai.entity.ExamplePrompt;
import com.datn.datnbe.ai.enums.ExamplePromptType;
import com.datn.datnbe.ai.repository.ExamplePromptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamplePromptManagement {

    private final ExamplePromptRepository examplePromptRepository;

    public List<ExamplePrompt> getExamplePrompts(ExamplePromptType type, String language) {
        String lang = (language != null && !language.isEmpty()) ? language : "vi";
        // Fallback or exact match? For now exact match on "vi" or "en".
        // If language passed is something else, maybe fallback to "vi"?
        // Let's implement simple query for now.
        return examplePromptRepository.findByTypeAndLanguage(type, lang);
    }
}
