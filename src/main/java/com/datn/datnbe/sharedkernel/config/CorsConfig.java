package com.datn.datnbe.sharedkernel.config;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
@Slf4j
public class CorsConfig implements WebMvcConfigurer {
    @Value("${app.cors.allowed-origins:}")
    private List<String> allowedOrigins;

    @Value("${app.cors.allowed-origin-patterns:}")
    private List<String> allowedOriginPatterns;

    @Value("${app.cors.allowed-methods:GET,POST,PUT,PATCH,DELETE,OPTIONS}")
    private String methods;

    @Value("${app.cors.allow-credentials:true}")
    private boolean allowCredentials;

    @Value("${app.cors.max-age:3600}")
    private long maxAge;

    @Value("${app.cors.exposed-headers:}")
    private String exposedHeaders;

    @Autowired
    private PermissionHeaderResponseWrapper permissionHeaderResponseWrapper;

    /**
     * Bean for Spring Security CORS configuration
     * This is used by Spring Security filters and runs before authentication
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        if (!allowedOrigins.isEmpty() || !allowedOriginPatterns.isEmpty()) {
            // Combine both explicit origins and patterns
            List<String> allPatterns = new ArrayList<>();
            
            // Add explicit origins
            if (!allowedOrigins.isEmpty()) {
                allPatterns.addAll(allowedOrigins);
                log.info("CORS configured with allowed origins: {}", allowedOrigins);
            }
            
            // Add patterns
            if (!allowedOriginPatterns.isEmpty()) {
                allPatterns.addAll(allowedOriginPatterns);
                log.info("CORS configured with allowed origin patterns: {}", allowedOriginPatterns);
            }
            
            // Use setAllowedOriginPatterns instead of setAllowedOrigins
            // This method properly handles both exact origins and patterns
            configuration.setAllowedOriginPatterns(allPatterns);
            configuration.setAllowCredentials(allowCredentials);
        } else {
            // No origins configured - block all CORS requests
            configuration.setAllowedOrigins(Arrays.asList());
            log.warn("CORS not configured - all cross-origin requests will be blocked");
        }

        configuration.setAllowedMethods(Arrays.asList(methods.split(",")));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setMaxAge(maxAge);

        if (exposedHeaders != null && !exposedHeaders.isBlank()) {
            configuration.setExposedHeaders(Arrays.asList(exposedHeaders.split(",")));
        }

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

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
