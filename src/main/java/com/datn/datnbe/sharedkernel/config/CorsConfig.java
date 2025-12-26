package com.datn.datnbe.sharedkernel.config;

import java.util.Arrays;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableConfigurationProperties(CorsProperties.class)
@Slf4j
public class CorsConfig {

    private final CorsProperties corsProperties;

    public CorsConfig(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
        log.info("CorsProperties loaded - origins: {}, patterns: {}",
                corsProperties.getAllowedOrigins(),
                corsProperties.getAllowedOriginPatterns());
    }

    /**
     * Bean for Spring Security CORS configuration
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        log.debug("CORS configuration - allowedOrigins: {}, allowedOriginPatterns: {}",
                corsProperties.getAllowedOrigins(),
                corsProperties.getAllowedOriginPatterns());

        boolean hasOrigins = corsProperties.getAllowedOrigins() != null
                && !corsProperties.getAllowedOrigins().isEmpty();
        boolean hasPatterns = corsProperties.getAllowedOriginPatterns() != null
                && !corsProperties.getAllowedOriginPatterns().isEmpty();

        if (hasOrigins) {
            configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());
            log.info("✓ CORS configured with exact origins: {}", corsProperties.getAllowedOrigins());
        }

        if (hasPatterns) {
            configuration.setAllowedOriginPatterns(corsProperties.getAllowedOriginPatterns());
            log.info("✓ CORS configured with origin patterns: {}", corsProperties.getAllowedOriginPatterns());
        }

        if (!hasOrigins && !hasPatterns) {
            log.warn("⚠ No CORS origins configured - using fallback: allow all origins");
            configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        }

        // Always allow credentials, headers, and all methods for preflight
        configuration.setAllowCredentials(corsProperties.isAllowCredentials());
        configuration.setAllowedMethods(Arrays.asList(corsProperties.getAllowedMethods().split(",")));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("*"));
        configuration.setMaxAge(corsProperties.getMaxAge());

        if (corsProperties.getExposedHeaders() != null && !corsProperties.getExposedHeaders().isBlank()) {
            configuration.setExposedHeaders(Arrays.asList(corsProperties.getExposedHeaders().split(",")));
        }

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        log.info("✓ CORS configuration bean created and registered for all paths");
        return source;
    }
}
