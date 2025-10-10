package com.datn.datnbe.auth.config;

import com.datn.datnbe.auth.config.properties.KeycloakProperties;
import lombok.RequiredArgsConstructor;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class KeycloakConfig {
    private final KeycloakProperties keycloakProps;

    @Bean
    public Keycloak keycloak() {
        return KeycloakBuilder.builder()
                .serverUrl(keycloakProps.getEndpoint())
                .realm(keycloakProps.getRealm())
                .grantType(OAuth2Constants.PASSWORD)
                .clientId(keycloakProps.getApplication().getClientId())
                .username(keycloakProps.getApplication().getClientUsername())
                .password(keycloakProps.getApplication().getClientPassword())
                .build();
    }

    @Bean
    public RealmResource realmResource(Keycloak keycloak) {
        return keycloak.realm(keycloakProps.getRealm());
    }

    @Bean
    public UsersResource userResource(RealmResource realmResource) {
        return realmResource.users();
    }
}
