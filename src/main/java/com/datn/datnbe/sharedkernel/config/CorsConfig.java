package com.datn.datnbe.sharedkernel.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.datn.datnbe.sharedkernel.security.response.PermissionHeaderResponseWrapper;

@Configuration
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

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        var mapping = registry.addMapping("/**").allowedMethods(methods.split(",")).allowedHeaders("*").maxAge(maxAge);

        // Prefer *either* exact origins or patterns
        if (!allowedOrigins.isEmpty()) {
            mapping.allowedOrigins(allowedOrigins.toArray(new String[0])).allowCredentials(allowCredentials);
        } else if (!allowedOriginPatterns.isEmpty()) {
            mapping.allowedOriginPatterns(allowedOriginPatterns.toArray(new String[0]))
                    .allowCredentials(allowCredentials);
        } else {
            // When using wildcard pattern, credentials must be false
            mapping.allowedOriginPatterns("*").allowCredentials(false);
        }

        if (exposedHeaders != null && !exposedHeaders.isBlank()) {
            mapping.exposedHeaders(exposedHeaders.split(","));
        }
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(permissionHeaderResponseWrapper);
    }
}
