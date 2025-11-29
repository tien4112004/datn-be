package com.datn.datnbe.auth.config;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

import com.datn.datnbe.auth.handler.CustomAuthenticationEntryPoint;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableWebSecurity
@Profile("!test & !integration-test")
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {
    private final JwtConverter jwtConverter;
    private final ObjectMapper objectMapper;
    private final CorsConfigurationSource corsConfigurationSource;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Allow OPTIONS requests (CORS preflight) without authentication - MUST be first
                        .requestMatchers(HttpMethod.OPTIONS, "/**")
                        .permitAll()

                        // Public endpoints - no authentication required (must come BEFORE /api/**)
                        .requestMatchers("/public/**", "/api/auth/**", "/api/resources/register", "/v3/**")
                        .permitAll()

                        // Public GET endpoints - allow anonymous access
                        .requestMatchers(HttpMethod.GET, "/api/models", "/api/models/**")
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
                .oauth2ResourceServer(
                        oauth2 -> oauth2.bearerTokenResolver(new CookieBearerTokenResolver("access_token"))
                                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter)))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(this::handleAccessDeniedException));

        return http.build();
    }

    private void handleAccessDeniedException(HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {
        log.warn("Access denied for path: {} by user {} - {}",
                request.getRequestURI(),
                request.getRemoteUser(),
                accessDeniedException.getMessage());

        response.setStatus(HttpStatus.NOT_FOUND.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        AppResponseDto<?> errorResponse = AppResponseDto.builder()
                .success(false)
                .code(HttpStatus.NOT_FOUND.value())
                .message("Not found resource!")
                .errorCode("NOT_FOUND")
                .build();

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
