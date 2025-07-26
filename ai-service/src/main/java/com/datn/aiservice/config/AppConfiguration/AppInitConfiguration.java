package com.datn.aiservice.config.AppConfiguration;

import java.util.Objects;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.datn.aiservice.config.ChatModelConfiguration.ModelProperties;
import com.datn.aiservice.repository.interfaces.ModelConfigurationRepo;
import com.datn.aiservice.service.interfaces.ModelSelectionService;

import jakarta.annotation.PostConstruct;
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
    @PostConstruct
    void InitializeModelConfiguration() {
        var models = modelSelectionService.getModelsConfiguration();
        var definedModels = modelProperties.getModels();

        if (!models.isEmpty() && models.size() == definedModels.size()) {
            log.info("All models are ready");
            return;
        }

        modelProperties.getModels().forEach((key, modelData) -> {
            var existedModel = modelSelectionService.getFullModelConfiguration(modelData.getModelName());
            if (Objects.isNull(existedModel))
                modelSelectionService.saveModelData(modelData);
        });
    }
}
