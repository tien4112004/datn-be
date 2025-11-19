package com.datn.datnbe.sharedkernel.config;

import java.util.Arrays;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.datn.datnbe.sharedkernel.security.response.PermissionHeaderResponseWrapper;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableConfigurationProperties(CorsProperties.class)
@Slf4j
public class CorsConfig implements WebMvcConfigurer {

    private final CorsProperties corsProperties;

    @Autowired
    private PermissionHeaderResponseWrapper permissionHeaderResponseWrapper;

    public CorsConfig(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
        log.info("CorsProperties loaded - origins: {}, patterns: {}",
                corsProperties.getAllowedOrigins(),
                corsProperties.getAllowedOriginPatterns());
    }

    /**
     * Bean for Spring Security CORS configuration
     * This is used by Spring Security filters and runs before authentication
     *
     * Spring Security's setAllowedOriginPatterns() expects Spring AntPathMatcher patterns:
     * - ? matches one character
     * - * matches zero or more characters within a segment
     * - ** matches zero or more directory levels
     *
     * NOTE: For URLs like https://datn-fe-container-abc123-datn-fe.vercel.app,
     * we use pattern like https://datn-fe-*.vercel.app
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        log.debug("CORS configuration - allowedOrigins: {}, allowedOriginPatterns: {}",
                corsProperties.getAllowedOrigins(),
                corsProperties.getAllowedOriginPatterns());

        // Check if we have any patterns or origins configured
        boolean hasOrigins = corsProperties.getAllowedOrigins() != null
                && !corsProperties.getAllowedOrigins().isEmpty();
        boolean hasPatterns = corsProperties.getAllowedOriginPatterns() != null
                && !corsProperties.getAllowedOriginPatterns().isEmpty();

        if (hasOrigins) {
            // Use setAllowedOrigins for exact matches
            configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());
            log.info("✓ CORS configured with exact origins: {}", corsProperties.getAllowedOrigins());
        }

        if (hasPatterns) {
            // Use setAllowedOriginPatterns for pattern-based matches
            configuration.setAllowedOriginPatterns(corsProperties.getAllowedOriginPatterns());
            log.info("✓ CORS configured with origin patterns: {}", corsProperties.getAllowedOriginPatterns());
        }

        if (!hasOrigins && !hasPatterns) {
            // Allow all origins as fallback if nothing is configured
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

    // Note: addCorsMappings is NOT needed since we're using Spring Security's CORS
    // via the corsConfigurationSource bean above. Having both causes duplicate headers.
    // Spring Security's CORS runs earlier in the filter chain and is sufficient.

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(permissionHeaderResponseWrapper);
    }
}
