package com.datn.datnbe.ai.config.appconfiguration;

import com.datn.datnbe.ai.api.ModelSelectionApi;
import com.datn.datnbe.ai.config.chatmodelconfiguration.ModelProperties;
import com.datn.datnbe.ai.dto.response.ModelResponseDto;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@ConditionalOnProperty(prefix = "app.init", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AppInitConfig {

    ModelProperties modelProperties;
    ModelSelectionApi modelSelectionApi;

    @Bean
    CommandLineRunner InitializeModelConfiguration() {
        return args -> {
            var existingModels = modelSelectionApi.getModelConfigurations();
            var existingModelNames = existingModels.stream()
                    .map(ModelResponseDto::getModelName)
                    .collect(Collectors.toSet());

            var configuredModels = modelProperties.getModels();
            var configuredModelNames = modelProperties.getModelNames();

            // Synchronize configured models with existing models
            configuredModels.forEach(model -> {
                if (!modelSelectionApi.existByName(model.getModelName())) {
                    log.info("Adding new model: {}", model.getModelName());
                    modelSelectionApi.saveModelInfo(model);
                } else {
                    log.debug("Model {} already exists, skipping initialization", model.getModelName());
                }
            });

            // Remove models that are no longer configured
            existingModelNames.stream()
                    .filter(modelName -> !configuredModelNames.contains(modelName))
                    .forEach(modelName -> {
                        log.info("Removing model {} as it's no longer in configuration", modelName);
                        modelSelectionApi.removeModelByName(modelName);
                    });

            log.info("Model configuration synchronization completed");
        };
    }
}
