package com.datn.aiservice.config;

import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelConfig {
    @Value("${spring.ai.openai.base-url}")
    private String openAiBaseUrl;

    @Value("${spring.ai.openai.api-key}")
    private String openAiApiKey;

    @Bean
    public OpenAiChatModel basedModel(
            OpenAiApi openAiApi
    ) {
        // you can even just return baseChatModel here, since its default is gpt-4o
        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(
                        OpenAiChatOptions.builder()
                                .model("gpt-4o")
                                .build())
                .build();
    }

    @Bean
    public OpenAiChatModel geminiFlashModel(
            OpenAiChatModel basedModel,
            OpenAiApi openAiApi) {
        return basedModel.mutate()
                .openAiApi(openAiApi)
                .defaultOptions(
                        OpenAiChatOptions.builder()
                                .model("gemini-2.0-flash")
                                .build())
                .build();
    }

    @Bean
    public OpenAiChatModel deepseekModel(
            OpenAiChatModel basedModel,
            OpenAiApi openAiApi) {
        return basedModel.mutate()
                .openAiApi(openAiApi)
                .defaultOptions(
                        OpenAiChatOptions.builder()
                                .model("deepseek-v1")
                                .build())
                .build();
    }

    @Bean
    public OpenAiApi openAiApi() {
        return OpenAiApi.builder()
                .apiKey(openAiApiKey)
                .baseUrl(openAiBaseUrl)
                .build();
    }
}
