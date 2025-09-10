package com.datn.datnbe.ai.config.chatmodelconfiguration;

import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

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
        boolean textCapable;
        boolean imageCapable;
        boolean videoCapable;
    }

    public List<ModelInfo> getModels() {
        return configurations.values().stream().flatMap(List::stream).toList();
    }

    public List<String> getModelNames() {
        return configurations.values().stream().flatMap(List::stream).map(ModelInfo::getModelName).toList();
    }
}
