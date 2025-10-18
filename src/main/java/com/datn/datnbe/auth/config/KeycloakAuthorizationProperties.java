package com.datn.datnbe.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@ConfigurationProperties(prefix = "app.auth.authorization")
public class KeycloakAuthorizationProperties {
    String serverUrl;

    String realm;

    String clientId;

    String clientSecret;

    String clientUuid;

    Integer connectionTimeout = 5000;

    Integer readTimeout = 30000;
}
