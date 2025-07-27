package com.datn.aiservice.factory;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ChatClientFactory {
    Map<String, ChatClient> chatClients;

    public ChatClient getChatClient(String modelName) {
        if (chatClients.containsKey(modelName)) {
            return chatClients.get(modelName);
        } else {
            throw new IllegalArgumentException("No ChatClient found for model: " + modelName);
        }
    }
}