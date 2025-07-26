package com.datn.aiservice.config.ChatModelConfiguration;

import lombok.Data;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Data
@ConfigurationProperties(prefix = "app.models")
@ConfigurationPropertiesScan
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class ModelProperties {
    Map<String, ModelInfo> configurations;

    @Data
    public static class ModelInfo {
        String modelName;
        String displayName;
        String provider;
        Integer maxTokens;
    }
}
