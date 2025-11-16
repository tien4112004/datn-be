package com.datn.datnbe.auth.config;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;

import com.datn.datnbe.auth.utils.CookieBearerTokenResolver;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

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
                // Allow OPTIONS requests (CORS preflight) without authentication
                .requestMatchers(HttpMethod.OPTIONS, "/**")
                .permitAll()

                // Public endpoints - no authentication required
                .requestMatchers("/public/**",
                        "/api/auth/signin",
                        "/api/auth/signup",
                        "/api/auth/exchange",
                        "/api/resources/register",
                        "/api/auth/google/signin",
                        "/api/auth/google/authorize",
                        "/v3/**")
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
                        .deleteCookies("JSESSIONID", "access_token", "refresh_token"))
                .oauth2ResourceServer(
                        oauth2 -> oauth2.bearerTokenResolver(new CookieBearerTokenResolver("access_token"))
                                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter)))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(
                        exceptions -> exceptions.authenticationEntryPoint(this::handleAuthenticationException)
                                .accessDeniedHandler(this::handleAccessDeniedException));

        http.csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"));

        return http.build();
    }

    private void handleAuthenticationException(HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {
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
            AccessDeniedException accessDeniedException) throws IOException {
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

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers("/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/api/auth/signin",
                        "/api/auth/signup",
                        "/api/auth/exchange",
                        "/api/resources/register",
                        "/api/auth/google/authorize");
    }

}
