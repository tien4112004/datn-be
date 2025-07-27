package com.datn.aiservice.config.chatmodelconfiguration;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class ModelConfig {
    final ModelProperties modelProperties;

    @Value("${spring.ai.openai.base-url}")
    String openAiBaseUrl;

    @Value("${spring.ai.openai.api-key}")
    String openAiApiKey;

    @Bean
    OpenAiApi openAiApi() {
        return OpenAiApi.builder()
                .apiKey(openAiApiKey)
                .baseUrl(openAiBaseUrl)
                .build();
    }

    @Bean
    OpenAiChatModel basedModel(
            OpenAiApi openAiApi) {

        var defaultModelConfig = getDefaultModelConfig();

        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(
                        OpenAiChatOptions.builder()
                                .maxTokens(defaultModelConfig.getMaxTokens())
                                .model(defaultModelConfig.getModelName())
                                .build())
                .build();
    }

    @Bean
    Map<String, OpenAiChatModel> allChatModels(OpenAiApi openAiApi) {
        log.info("Configuring all chat models with base URL: {} and API Key: {}", openAiBaseUrl, openAiApiKey);
        Map<String, OpenAiChatModel> models = new HashMap<>();

        modelProperties.getConfigurations().forEach((key, config) -> {
            log.info("Configuring model: {} with config {}", key, config);
            OpenAiChatModel model = OpenAiChatModel.builder()
                    .openAiApi(openAiApi)
                    .defaultOptions(
                            OpenAiChatOptions.builder()
                                    .model(config.getModelName())
                                    .maxTokens(config.getMaxTokens())
                                    .build())
                    .build();

            models.put(config.getModelName(), model);
        });

        return models;
    }

    private ModelProperties.ModelInfo getDefaultModelConfig() {
        return modelProperties.getConfigurations().values().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No default model configured"));
    }
}
