package com.datn.datnbe.auth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@Profile("!test & !integration-test")
@RequiredArgsConstructor
public class SecurityConfig {
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final JwtConverter jwtConverter;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        var oidcLogoutHandler = new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);

        http.authorizeHttpRequests(auth -> auth
                // Public endpoints - no authentication required
                .requestMatchers("/public", "/api/auth/signin", "/api/auth/signup")
                .permitAll()

                // Admin endpoints - requires admin role
                .requestMatchers("/api/admin/**")
                .hasRole("admin")

                // File permission endpoints - only requires authentication (any valid JWT)
                .requestMatchers("/api/files/register")
                .authenticated()
                .requestMatchers("/api/files/*/share")
                .authenticated()
                .requestMatchers("/api/files/*/permissions")
                .authenticated()
                .requestMatchers("/api/files/*/content")
                .authenticated()
                .requestMatchers("/api/files/*/download")
                .authenticated()
                .requestMatchers("/api/files/*/update")
                .authenticated()
                .requestMatchers("/api/files/*/delete")
                .authenticated()
                .requestMatchers("/api/files/*/share-with-others")
                .authenticated()

                // All other API endpoints - requires user role
                .requestMatchers("/api/**")
                .hasRole("user")

                // Everything else requires authentication
                .anyRequest()
                .authenticated())
                .oauth2Login(Customizer.withDefaults())
                .logout(l -> l.logoutSuccessHandler(oidcLogoutHandler))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter)));

        http.csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"));

        return http.build();
    }
}
