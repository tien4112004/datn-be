package com.datn.aiservice.config.chatmodelconfiguration;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SystemPromptConfig {

    @NonFinal
    @Value("classpath:prompts/default-system-prompt.st")
    Resource defaultSystemPrompt;

    @NonFinal
    @Value("classpath:prompts/slide-generation/outline-prompt.st")
    Resource outlinePrompt;

    @NonFinal
    @Value("classpath:prompts/slide-generation/slide-prompt.st")
    Resource slidePrompt;

    @Bean(name = "defaultSystemPrompt")
    public Resource commonRequirementsPrompt() {
        return defaultSystemPrompt;
    }

    @Bean(name = "outlinePromptResource")
    public Resource outlinePromptResource() {
        return outlinePrompt;
    }

    @Bean(name = "slidePromptResource")
    public Resource slidePromptResource() {
        return slidePrompt;
    }
}
