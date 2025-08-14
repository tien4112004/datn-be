package com.datn.datnbe.ai.config.chatmodelconfiguration;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class ChatClientConfig {
    final ModelProperties modelProperties;
    final Map<String, OpenAiChatModel> allChatModels;
    final Map<String, VertexAiGeminiChatModel> allGeminiChatModels;

    @Bean
    public Map<String, ChatClient> chatClients(@Qualifier("defaultSystemPrompt") Resource defaultSystemPrompt) {

        Map<String, ChatClient> clients = new HashMap<>();
        List<String> errorModels = new ArrayList<>();

        modelProperties.getConfigurations().forEach((providerKey, infos) -> {
            infos.forEach(info -> {
                log.info("Build Chat Client for model: {} from provider: {}", info.getModelName(), providerKey);
                ChatModel underlying = switch (providerKey) {
                    case "openai" -> allChatModels.get(info.getModelName());
                    case "gemini" -> allGeminiChatModels.get(info.getModelName());
                    default -> null;
                };

                if (underlying != null) {
                    ChatClient client = ChatClient.builder(underlying).defaultSystem(defaultSystemPrompt).build();
                    clients.put(info.getModelName(), client);
                } else {
                    errorModels.add(info.getModelName());
                }
            });
        });

        if (!errorModels.isEmpty()) {
            log.error("Failed to create chat clients for models: {}", errorModels);
        } else {
            log.info("Chat clients created successfully for all models.");
        }

        return clients;
    }
}
