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
@ConfigurationProperties(prefix = "app.auth.properties")
public class AuthProperties {
    String realm;
    String issuer;
    String serverUrl;
    String clientId;
    String clientSecret;
    String redirectUri;
    String signInUri;
    String signUpUri;
    String logoutUri;
    String tokenUri;
}
