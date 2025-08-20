package com.datn.datnbe.ai.config.chatmodelconfiguration;

import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "app.models")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Component
public class ModelProperties {
    Map<String, List<ModelInfo>> configurations;

    @Data
    public static class ModelInfo {
        String modelName;
        String displayName;
        String provider;
        boolean defaultModel;
    }

    public List<ModelInfo> getModels() {
        return configurations.values().stream().flatMap(List::stream).toList();
    }
}
