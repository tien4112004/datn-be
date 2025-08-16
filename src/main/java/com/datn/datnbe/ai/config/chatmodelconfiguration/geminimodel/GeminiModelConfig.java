package com.datn.datnbe.ai.config.chatmodelconfiguration.geminimodel;

import com.datn.datnbe.ai.config.chatmodelconfiguration.ModelProperties;
import com.google.cloud.vertexai.VertexAI;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class GeminiModelConfig {
    final ModelProperties modelProperties;
    private static final String GEMINI = "gemini";

    @Value("${spring.ai.vertex.ai.gemini.project-id}")
    String vertexProjectId;

    @Value("${spring.ai.vertex.ai.gemini.location}")
    String vertexProjectLocation;

    @Bean
    VertexAI vertexAI() {
        return new VertexAI(vertexProjectId, vertexProjectLocation);
    }

    @Bean
    public Map<String, VertexAiGeminiChatModel> allGeminiChatModels(VertexAI vertexAI) {
        return modelProperties.getConfigurations()
                .getOrDefault(GEMINI, List.of())
                .stream()
                .collect(Collectors.toMap(ModelProperties.ModelInfo::getModelName,
                        info -> VertexAiGeminiChatModel.builder()
                                .vertexAI(vertexAI)
                                .defaultOptions(VertexAiGeminiChatOptions.builder().model(info.getModelName()).build())
                                .build()));
    }
}
