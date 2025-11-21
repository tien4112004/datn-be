package com.datn.datnbe.sharedkernel.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Component
@ConfigurationProperties(prefix = "app.cors")
@Data
@Slf4j
public class CorsProperties {
    private List<String> allowedOrigins = new ArrayList<>();
    private List<String> allowedOriginPatterns = new ArrayList<>();
    private String allowedMethods = "GET,POST,PUT,PATCH,DELETE,OPTIONS";
    private boolean allowCredentials = true;
    private long maxAge = 3600;
    private String exposedHeaders = "";

    /**
     * Support for ALLOWED_ORIGINS environment variable
     * Format: comma-separated list of origins
     * Example: ALLOWED_ORIGINS=https://api.huy-devops.site,https://localhost:3000
     */
    @Value("${ALLOWED_ORIGINS:}")
    public void setAllowedOriginsFromEnv(String allowedOriginsEnv) {
        if (allowedOriginsEnv != null && !allowedOriginsEnv.trim().isEmpty()) {
            List<String> envOrigins = Arrays.asList(allowedOriginsEnv.split(","));
            if (!envOrigins.isEmpty()) {
                this.allowedOrigins.addAll(envOrigins);
                log.info("Added origins from ALLOWED_ORIGINS env var: {}", envOrigins);
            }
        }
    }
}
