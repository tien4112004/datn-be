package com.datn.datnbe.document.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "pexels.api")
@Data
public class PexelsConfig {
    private String key;
    private String baseUrl;
    private RateLimit rateLimit;

    @Data
    public static class RateLimit {
        private Integer requests;
        private Integer durationMinutes;
    }
}
