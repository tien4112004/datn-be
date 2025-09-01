package com.datn.datnbe.ai.config.chatmodelconfiguration;

import com.google.cloud.vertexai.api.EndpointName;
import com.google.cloud.vertexai.api.PredictionServiceClient;
import com.google.cloud.vertexai.api.PredictionServiceSettings;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.IOException;

@Configuration
@Profile("!test")
@ConditionalOnProperty(prefix = "app.gcloud", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ImageModelConfig {

    @Value("${app.gcloud.project-id}")
    private String projectId;

    @Value("${app.gcloud.location}")
    private String location;

    @Value("${app.gcloud.endpoint}")
    private String endpoint;

    @Bean
    public PredictionServiceClient predictionServiceClient() {
        try {
            final String vertexEndpoint = String.format("%s-aiplatform.googleapis.com:443", location);
            PredictionServiceSettings predictionServiceSettings = PredictionServiceSettings.newBuilder()
                    .setEndpoint(vertexEndpoint).build();

            return PredictionServiceClient.create(predictionServiceSettings);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create PredictionServiceClient", e);
        }
    }

    @Bean
    public EndpointName imageEndpointName() {
        // Hardcode model for now
        return EndpointName.ofProjectLocationPublisherModelName(
                projectId, location, "google", "imagen-3.0-generate-001");
    }

    @ConfigurationProperties(prefix = "app.gcloud")
    @Data
    public static class GcloudProperties {
        private String projectId;
        private String location;
        private String endpoint;
    }
}
