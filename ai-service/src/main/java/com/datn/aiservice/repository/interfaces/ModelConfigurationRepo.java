package com.datn.aiservice.repository.interfaces;

import org.springframework.ai.chat.client.ChatClient;

public interface ModelConfigurationRepo {

    String getModelName();

    boolean isModelEnabled(String modelName);

}
