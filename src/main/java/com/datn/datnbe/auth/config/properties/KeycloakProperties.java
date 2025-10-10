package com.datn.datnbe.auth.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(value = "app.auth.keycloak")
public class KeycloakProperties {
    private String realm;
    private String endpoint;
    private final Application application = new Application();

    @Getter
    @Setter
    public static class Application {
        private String clientId;
        private String clientUsername;
        private String clientPassword;
    }
}
