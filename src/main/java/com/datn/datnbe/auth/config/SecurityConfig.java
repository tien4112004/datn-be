package com.datn.datnbe.auth.config;

import java.io.IOException;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;

import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@Profile("!test & !integration-test")
@RequiredArgsConstructor
public class SecurityConfig {
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final JwtConverter jwtConverter;
    private final ObjectMapper objectMapper;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        var oidcLogoutHandler = new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);

        http.authorizeHttpRequests(auth -> auth
                // Public endpoints - no authentication required
                .requestMatchers("/public/**",
                        "/api/auth/signin",
                        "/api/auth/signup",
                        "/api/auth/keycloak/callback",
                        "/api/resources/register")
                .permitAll()

                // Admin endpoints - requires ADMIN role
                .requestMatchers("/api/admin/**")
                .hasRole("admin")

                // Resource endpoints - authenticated users only
                .requestMatchers("/api/resources/**")
                .authenticated()

                // API endpoints - requires USER role
                .requestMatchers("/api/**")
                .hasAnyRole("user", "admin")

                // Default deny
                .anyRequest()
                .authenticated())
                .oauth2Login(oauth2 -> oauth2.defaultSuccessUrl("/", true))
                .logout(logout -> logout.logoutSuccessHandler(oidcLogoutHandler)
                        .logoutUrl("/api/auth/logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID"))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter)))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
                .exceptionHandling(
                        exceptions -> exceptions.authenticationEntryPoint(this::handleAuthenticationException)
                                .accessDeniedHandler(this::handleAccessDeniedException));

        return http.build();
    }

    private void handleAuthenticationException(HttpServletRequest request,
            HttpServletResponse response,
            org.springframework.security.core.AuthenticationException authException) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        AppResponseDto<?> errorResponse = AppResponseDto.builder()
                .success(false)
                .code(HttpStatus.UNAUTHORIZED.value())
                .message("Authentication required: " + authException.getMessage())
                .errorCode("UNAUTHORIZED")
                .build();

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }

    private void handleAccessDeniedException(HttpServletRequest request,
            HttpServletResponse response,
            org.springframework.security.access.AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        AppResponseDto<?> errorResponse = AppResponseDto.builder()
                .success(false)
                .code(HttpStatus.FORBIDDEN.value())
                .message("Access denied: " + accessDeniedException.getMessage())
                .errorCode("FORBIDDEN")
                .build();

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
