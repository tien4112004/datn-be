package com.datn.datnbe.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;

/**
 * Configuration for Keycloak Authorization Services HTTP client.
 * This configuration creates a dedicated RestTemplate for making
 * REST API calls to Keycloak's Protection API.
 */
@Configuration
@RequiredArgsConstructor
public class KeycloakAuthorizationClientConfig {
    private final KeycloakAuthorizationProperties authzProperties;

    /**
     * Creates a RestTemplate configured for Keycloak Authorization API calls.
     * This RestTemplate is used specifically for interacting with Keycloak's
     * Protection API endpoints for resource, policy, and permission management.
     *
     * @return Configured RestTemplate instance
     */
    @Bean(name = "keycloakAuthorizationRestTemplate")
    public RestTemplate keycloakAuthorizationRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(clientHttpRequestFactory());
        return restTemplate;
    }

    /**
     * Creates a ClientHttpRequestFactory with configured timeouts.
     *
     * @return ClientHttpRequestFactory instance
     */
    private ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(authzProperties.getConnectionTimeout());
        factory.setReadTimeout(authzProperties.getReadTimeout());
        return factory;
    }
}
