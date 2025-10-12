package com.datn.datnbe.auth.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.auth.properties")
public class AuthProperties {
    private String issuer;
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String signInUri;
    private String signUpUri;
    private String logoutUri;
    private String tokenUri;
}
