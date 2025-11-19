package com.datn.datnbe.auth.config;

import java.io.IOException;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

import com.datn.datnbe.auth.handler.CustomAuthenticationEntryPoint;
import com.datn.datnbe.auth.utils.CookieBearerTokenResolver;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableWebSecurity
@Profile("!test & !integration-test")
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final JwtConverter jwtConverter;
    private final ObjectMapper objectMapper;
    private final CorsConfigurationSource corsConfigurationSource;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        var oidcLogoutHandler = new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);

        http.cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
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
                .logout(logout -> logout.logoutSuccessHandler(oidcLogoutHandler)
                        .logoutUrl("/api/auth/logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "access_token", "refresh_token"))
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
        response.setStatus(HttpStatus.NOT_FOUND.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // Add CORS headers to error response so browser can receive it
        addCorsHeaders(request, response);

        AppResponseDto<?> errorResponse = AppResponseDto.builder()
                .success(false)
                .code(HttpStatus.NOT_FOUND.value())
                .message("Not found resource!")
                .errorCode("NOT_FOUND")
                .build();

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }

    private void addCorsHeaders(HttpServletRequest request, HttpServletResponse response) {
        String origin = request.getHeader("Origin");
        if (origin != null) {
            // Check if origin matches any of the allowed patterns
            org.springframework.web.cors.CorsConfiguration config = corsConfigurationSource
                    .getCorsConfiguration(request);
            if (config != null) {
                boolean originAllowed = false;

                // Check exact origins first
                if (config.getAllowedOrigins() != null && config.getAllowedOrigins().contains(origin)) {
                    originAllowed = true;
                } else if (config.getAllowedOriginPatterns() != null) {
                    // Check with Spring's pattern matcher (supports *, **, ?)
                    org.springframework.util.AntPathMatcher matcher = new org.springframework.util.AntPathMatcher();
                    for (String pattern : config.getAllowedOriginPatterns()) {
                        if (pattern.equals("*") || matcher.match(pattern, origin)) {
                            originAllowed = true;
                            break;
                        }
                    }
                }

                if (originAllowed) {
                    response.setHeader("Access-Control-Allow-Origin", origin);
                    response.setHeader("Access-Control-Allow-Methods", String.join(", ", config.getAllowedMethods()));
                    response.setHeader("Access-Control-Allow-Headers",
                            config.getAllowedHeaders() != null && config.getAllowedHeaders().contains("*")
                                    ? request.getHeader("Access-Control-Request-Headers") != null
                                            ? request.getHeader("Access-Control-Request-Headers")
                                            : "*"
                                    : String.join(", ", config.getAllowedHeaders()));

                    if (config.getAllowCredentials()) {
                        response.setHeader("Access-Control-Allow-Credentials", "true");
                    }

                    List<String> exposedHeaders = config.getExposedHeaders();
                    if (exposedHeaders != null && !exposedHeaders.isEmpty()) {
                        response.setHeader("Access-Control-Expose-Headers", String.join(", ", exposedHeaders));
                    }
                }
            }
        }
    }
}
