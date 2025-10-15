package com.datn.datnbe.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/**
 * Configuration properties for Keycloak Authorization Services.
 * These properties are used to interact with Keycloak's Protection API
 * for managing resources, policies, and permissions.
 */
@Getter
@Setter
@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@ConfigurationProperties(prefix = "app.auth.authorization")
public class KeycloakAuthorizationProperties {
    /**
     * The Keycloak server URL (e.g., http://localhost:8082)
     */
    String serverUrl;

    /**
     * The realm name (e.g., ai-primary-dev)
     */
    String realm;

    /**
     * The client ID for authorization services (e.g., ai-primary)
     */
    String clientId;

    /**
     * The client secret for authorization services
     */
    String clientSecret;

    /**
     * Connection timeout in milliseconds (default: 5000)
     */
    Integer connectionTimeout = 5000;

    /**
     * Read timeout in milliseconds (default: 30000)
     */
    Integer readTimeout = 30000;
}
