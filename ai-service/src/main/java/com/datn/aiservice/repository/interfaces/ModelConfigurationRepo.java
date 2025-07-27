package com.datn.aiservice.repository.interfaces;

import org.springframework.ai.chat.client.ChatClient;

import com.datn.aiservice.entity.ModelConfigurationEntity;

import java.util.List;
import java.util.Map;

public interface ModelConfigurationRepo {

    String getModelName();

    boolean isModelEnabled(String modelName);

    List<ModelConfigurationEntity> getModels();

    void addModel(String modelName, ChatClient chatClient);

    void addModels(Map<String, ChatClient> chatClients);
}
