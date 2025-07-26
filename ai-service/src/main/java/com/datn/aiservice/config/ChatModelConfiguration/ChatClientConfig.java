package com.datn.aiservice.config.ChatModelConfiguration;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class ChatClientConfig {

    @Bean("gpt4oModel")
    public ChatClient gpt4oClient(
            @Qualifier("basedModel") OpenAiChatModel m,
            Resource defaultSystemPrompt) {
        return ChatClient
                .builder(m)
                .defaultSystem(defaultSystemPrompt)
                .build();
    }

    @Bean("gemini2.0FlashModel")
    public ChatClient geminiFlashClient(
            @Qualifier("geminiFlashModel") OpenAiChatModel m,
            Resource defaultSystemPrompt) {
        return ChatClient
                .builder(m)
                .defaultSystem(defaultSystemPrompt)
                .build();
    }

    @Bean("deepseekChatClient")
    public ChatClient deepseekClient(
            @Qualifier("deepseekModel") OpenAiChatModel m,
            Resource defaultSystemPrompt) {
        return ChatClient
                .builder(m)
                .defaultSystem(defaultSystemPrompt)
                .build();
    }

}
