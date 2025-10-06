package com.datn.datnbe.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth.requestMatchers("/", "/public")
                .permitAll()
                .requestMatchers("/api/admin/**")
                .hasRole("admin")
                .requestMatchers("/api/**")
                .hasRole("user")
                .anyRequest()
                .authenticated())
                .oauth2Login(Customizer.withDefaults())      // OIDC login via Keycloak
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter()) // roles from token
                ));

        // CSRF defaults are fine for form-based; if you're pure API, consider disabling or using tokens.
        return http.build();
    }

    private JwtAuthenticationConverter jwtAuthConverter() {
        // Extract Keycloak realm roles from realm_access.roles → ROLE_*
        JwtGrantedAuthoritiesConverter delegate = new JwtGrantedAuthoritiesConverter();
        delegate.setAuthoritiesClaimName("realm_access.roles");
        delegate.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(delegate);
        return converter;
    }
}
