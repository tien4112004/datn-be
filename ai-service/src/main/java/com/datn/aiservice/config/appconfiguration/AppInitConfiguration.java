package com.datn.aiservice.config.appconfiguration;

import java.util.Objects;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.datn.aiservice.config.chatmodelconfiguration.ModelProperties;
import com.datn.aiservice.service.interfaces.ModelSelectionService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AppInitConfiguration {
    ModelProperties modelProperties;
    ModelSelectionService modelSelectionService;

    @Bean
    CommandLineRunner InitializeModelConfiguration() {
        return args -> {
            var models = modelSelectionService.getModelsConfiguration();
            var definedModels = modelProperties.getConfigurations();

            if (models.isEmpty() || models.size() != definedModels.size()) {
                modelProperties.getConfigurations().forEach((key, modelData) -> {
                    var existedModel = modelSelectionService.getFullModelConfiguration(modelData.getModelName());
                    if (Objects.isNull(existedModel))
                        modelSelectionService.saveModelData(modelData);
                });
            } else {
                log.info("All models are ready");
            }
        };
    }
}
