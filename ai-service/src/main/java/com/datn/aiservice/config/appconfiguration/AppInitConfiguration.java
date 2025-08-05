package com.datn.aiservice.config.appconfiguration;

import com.datn.aiservice.config.chatmodelconfiguration.ModelProperties;
import com.datn.aiservice.service.interfaces.ModelSelectionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@ConditionalOnProperty(prefix = "app.init", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AppInitConfiguration {
    ModelProperties modelProperties;
    ModelSelectionService modelSelectionService;

    @Bean
    CommandLineRunner InitializeModelConfiguration() {
        return args -> {
            var models = modelSelectionService.getModelConfigurations();
            var definedModels = modelProperties.getConfigurations();

            if (models.isEmpty() || models.size() != definedModels.size()) {
                modelProperties.getModels().forEach((model)-> {
                    if (!modelSelectionService.existByName(model.getModelName())) {
                        modelSelectionService.saveModelInfo(model);
                    } else {
                        log.info("Model {} already exists, skipping initialization", model.getModelName());
                    }
                });
            } else {
                log.info("All models are ready");
            }
        };
    }
}
