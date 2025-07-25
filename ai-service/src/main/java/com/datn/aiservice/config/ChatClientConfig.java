package com.datn.aiservice.config;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.model.chat.client.autoconfigure.ChatClientBuilderConfigurer;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ChatClientConfig {

    @NonFinal
    @Value("classpath:prompts/default-system-prompt.st")
    Resource defaultSystemPrompt;

    @NonFinal
    @Value("classpath:prompts/slide-generation/outline-prompt.st")
    Resource outlinePrompt;

    @NonFinal
    @Value("classpath:prompts/slide-generation/slide-prompt.st")
    Resource slidePrompt;

    @Bean("gpt4oModel")
    public ChatClient gpt4oClient(@Qualifier("basedModel") OpenAiChatModel m) {
        return ChatClient
                .builder(m)
                .defaultSystem(defaultSystemPrompt)
                .build();
    }

    @Bean("gemini2.0FlashModel")
    public ChatClient geminiFlashClient(@Qualifier("geminiFlashModel") OpenAiChatModel m) {
        return ChatClient
                .builder(m)
                .defaultSystem(defaultSystemPrompt)
                .build();
    }

    @Bean("deepseekModel")
    public ChatClient deepseekClient(@Qualifier("deepseekModel") OpenAiChatModel m) {
        return ChatClient
                .builder(m)
                .defaultSystem(defaultSystemPrompt)
                .build();
    }

    @Bean
    public ChatClientBuilderConfigurer configurer() {
        return new ChatClientBuilderConfigurer();
    }

    @Bean(name = "commonRequirementsPromptResource")
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
