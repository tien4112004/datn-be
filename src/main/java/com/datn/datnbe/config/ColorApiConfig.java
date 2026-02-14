package com.datn.datnbe.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for external Color API integration
 * Supports TheColorAPI (https://www.thecolorapi.com) for color name resolution
 */
@Component
@ConfigurationProperties(prefix = "external-api.color-api")
@Data
public class ColorApiConfig {
    /**
     * Base URL for TheColorAPI service
     */
    private String baseUrl = "https://www.thecolorapi.com";

    /**
     * Timeout for API requests in milliseconds
     */
    private Integer timeout = 5000;

    /**
     * Feature flag to enable/disable Color API integration
     */
    private Boolean enabled = true;
}
