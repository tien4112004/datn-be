package com.datn.datnbe.auth.handler;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfigurationSource;

import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;
    private final CorsConfigurationSource corsConfigurationSource;

    @Override
    public void commence(HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {

        // CRITICAL: Add CORS headers FIRST before any response is written
        addCorsHeaders(request, response);

        String origin = request.getHeader("Origin");
        String allowedOrigin = response.getHeader("Access-Control-Allow-Origin");

        // Check if this is a CORS-related error
        // If there's an Origin header but no Access-Control-Allow-Origin in the response,
        // it means CORS blocked the request
        if (origin != null && !origin.isEmpty() && (allowedOrigin == null || allowedOrigin.isEmpty())) {

            log.warn("CORS policy violation - Origin '{}' is not allowed to access {} {}",
                    origin,
                    request.getMethod(),
                    request.getRequestURI());

            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            AppResponseDto<?> errorResponse = AppResponseDto.builder()
                    .success(false)
                    .code(HttpStatus.FORBIDDEN.value())
                    .message("Origin '" + origin + "' is not allowed by CORS policy")
                    .errorCode("CORS_ORIGIN_BLOCKED")
                    .build();

            objectMapper.writeValue(response.getOutputStream(), errorResponse);

        } else {
            // Normal authentication error
            log.debug("Authentication failed for {} {}: {}",
                    request.getMethod(),
                    request.getRequestURI(),
                    authException.getMessage());

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
    }

    private void addCorsHeaders(HttpServletRequest request, HttpServletResponse response) {
        String origin = request.getHeader("Origin");
        if (origin != null) {
            org.springframework.web.cors.CorsConfiguration config = corsConfigurationSource
                    .getCorsConfiguration(request);
            if (config != null) {
                boolean originAllowed = false;

                // Check exact origins
                if (config.getAllowedOrigins() != null && config.getAllowedOrigins().contains(origin)) {
                    originAllowed = true;
                } else if (config.getAllowedOriginPatterns() != null) {
                    // Check patterns with AntPathMatcher
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

                    if (config.getAllowCredentials() != null && config.getAllowCredentials()) {
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
