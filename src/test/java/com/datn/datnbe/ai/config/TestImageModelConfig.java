package com.datn.datnbe.ai.config;


import com.google.cloud.vertexai.api.EndpointName;
import com.google.cloud.vertexai.api.PredictionServiceClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import static org.mockito.Mockito.mock;

@TestConfiguration
@Profile("test")
public class TestImageModelConfig {

    @Bean
    @Primary
    public PredictionServiceClient testPredictionServiceClient() {
        return mock(PredictionServiceClient.class);
    }

    @Bean
    @Primary
    public EndpointName testImageEndpointName() {
        return EndpointName.ofProjectLocationPublisherModelName(
                "test-project", "us-central1", "google", "test-model");
    }
}