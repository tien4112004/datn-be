package com.datn.datnbe.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class KeycloakAuthorizationClientConfig {
    private final KeycloakAuthorizationProperties authzProperties;

    @Bean(name = "keycloakAuthorizationRestTemplate")
    public RestTemplate keycloakAuthorizationRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(clientHttpRequestFactory());
        return restTemplate;
    }

    private ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(authzProperties.getConnectionTimeout());
        factory.setReadTimeout(authzProperties.getReadTimeout());
        return factory;
    }
}
