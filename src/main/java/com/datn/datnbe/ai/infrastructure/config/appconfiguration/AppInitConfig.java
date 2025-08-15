package com.datn.datnbe.ai.infrastructure.config.appconfiguration;


import com.datn.datnbe.ai.api.ModelSelectionApi;
import com.datn.datnbe.ai.infrastructure.config.chatmodelconfiguration.ModelProperties;
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
            var models = modelSelectionApi.getModelConfigurations();
            var definedModels = modelProperties.getConfigurations();

            if (models.isEmpty() || models.size() != definedModels.size()) {
                modelProperties.getModels().forEach((model) -> {
                    if (!modelSelectionApi.existByName(model.getModelName())) {
                        modelSelectionApi.saveModelInfo(model);
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
