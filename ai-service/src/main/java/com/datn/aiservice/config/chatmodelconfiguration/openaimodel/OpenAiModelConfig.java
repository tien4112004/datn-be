package com.datn.aiservice.config.chatmodelconfiguration.openaimodel;

import com.datn.aiservice.config.chatmodelconfiguration.ModelProperties;
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
public class OpenAiModelConfig {
    final ModelProperties modelProperties;

    final String OPEN_AI = "openai";

    @Value("${spring.ai.openai.api-key}")
    String openAiApiKey;

    @Bean
    OpenAiApi openAiApi() {
        return OpenAiApi.builder().apiKey(openAiApiKey).build();
    }

    @Bean
    OpenAiChatModel basedModel(OpenAiApi openAiApi) {

        var defaultModelConfig = getDefaultModelConfig();

        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(OpenAiChatOptions.builder().model(defaultModelConfig.getModelName()).build())
                .build();
    }

    @Bean
    Map<String, OpenAiChatModel> allChatModels(OpenAiApi openAiApi) {
        Map<String, OpenAiChatModel> models = new HashMap<>();

        var listModels = modelProperties.getConfigurations().get(OPEN_AI);

        listModels.forEach(modelInfo -> {
            log.info("Configuring model: {}", modelInfo.getDisplayName());
            OpenAiChatModel model = OpenAiChatModel.builder()
                    .openAiApi(openAiApi)
                    .defaultOptions(OpenAiChatOptions.builder().model(modelInfo.getModelName()).build())
                    .build();

            models.put(modelInfo.getModelName(), model);
        });

        return models;
    }

    private ModelProperties.ModelInfo getDefaultModelConfig() {
        return modelProperties.getConfigurations()
                .get(OPEN_AI)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No default model configured"));
    }
}
