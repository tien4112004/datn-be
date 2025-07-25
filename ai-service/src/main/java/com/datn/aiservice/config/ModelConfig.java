package com.datn.aiservice.config;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ModelConfig {
    @Bean
    public OpenAiChatModel basedModel() {
        // you can even just return baseChatModel here, since its default is gpt-4o
        return OpenAiChatModel.builder()
                .defaultOptions(
                        OpenAiChatOptions.builder()
                                .model("gpt-4o")
                                .build()
                )
                .build();
    }

    @Bean
    public OpenAiChatModel geminiFlashModel(OpenAiChatModel basedModel) {
        return basedModel.mutate()
                .defaultOptions(
                        OpenAiChatOptions.builder()
                                .model("gemini-2.0-flash")
                                .build()
                )
                .build();
    }

    @Bean
    public OpenAiChatModel deepseekModel(OpenAiChatModel basedModel) {
        return basedModel.mutate()
                .defaultOptions(
                        OpenAiChatOptions.builder()
                                .model("deepseek-v1")
                                .build()
                )
                .build();
    }
}
