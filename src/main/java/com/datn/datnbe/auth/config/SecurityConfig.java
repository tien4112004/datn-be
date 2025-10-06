package com.datn.datnbe.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;

import lombok.RequiredArgsConstructor;

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

        http.authorizeHttpRequests(auth -> auth.requestMatchers("/public", "/api/auth/signin", "/api/auth/signup")
                .permitAll()
                .requestMatchers("/api/admin/**")
                .hasRole("admin")
                .requestMatchers("/api/**")
                .hasRole("user")
                .anyRequest()
                .authenticated())
                .oauth2Login(Customizer.withDefaults())
                .logout(l -> l.logoutSuccessHandler(oidcLogoutHandler))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter)));

        http.csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"));

        return http.build();
    }
}
