package com.datn.aiservice.repository.impl;

import com.datn.aiservice.entity.ModelConfigurationEntity;
import com.datn.aiservice.repository.impl.jpa.ModelConfigurationJPARepo;
import com.datn.aiservice.repository.interfaces.ModelConfigurationRepo;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ModelConfigurationRepoImpl implements ModelConfigurationRepo {

    ModelConfigurationJPARepo modelConfigurationJPARepo;

    // Example method implementation
    @Override
    public String getModelName() {
        return "Default Model Name"; // Replace with actual logic
    }

    @Override
    public boolean isModelEnabled(String modelName) {
        // Here you would typically check the model configuration in the database
        // For demonstration purposes, let's assume all models are enabled
        return true;
    }

    @Override
    public List<ModelConfigurationEntity> getModels() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getNumberModels'");
    }

    @Override
    public void addModel(String modelName, ChatClient chatClient) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addModel'");
    }

    @Override
    public void addModels(Map<String, ChatClient> chatClients) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addModels'");
    }
}
