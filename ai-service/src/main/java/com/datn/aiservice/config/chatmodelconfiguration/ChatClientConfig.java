package com.datn.aiservice.config.chatmodelconfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ChatClientConfig {
    private final ModelProperties modelProperties;

    @Bean
    public Map<String, ChatClient> chatClients(
            Map<String, OpenAiChatModel> allChatModels,
            @Qualifier("defaultSystemPrompt") Resource defaultSystemPrompt) {

        List<String> errorModels = new ArrayList<>();
        Map<String, ChatClient> chatClients = new HashMap<>();

        modelProperties.getConfigurations().forEach((key, config) -> {
            OpenAiChatModel model = allChatModels.get(config.getModelName());
            if (model != null) {
                ChatClient chatClient = ChatClient.builder(model)
                        .defaultSystem(defaultSystemPrompt)
                        .build();

                chatClients.put(config.getModelName(), chatClient);
            } else {
                errorModels.add(config.getModelName());
            }
        });

        if (!errorModels.isEmpty()) {
            log.error("Failed to create chat clients for models: {}", errorModels);
        } else {
            log.info("Chat clients created successfully for all models.");
        }

        return chatClients;
    }
}
