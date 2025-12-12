package com.datn.datnbe.auth.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.util.AntPathMatcher;

@Slf4j
public class CookieBearerTokenResolver implements BearerTokenResolver {
    private final DefaultBearerTokenResolver delegate = new DefaultBearerTokenResolver();
    private final String cookieName;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final String[] PUBLIC_ENDPOINTS = {"/public/**", "/api/auth/**", "/api/resources/register", "/v3/**",
            "/api/models", "/api/models/**", "/api/slide-themes", "/api/slide-themes/**", "/api/slide-templates",
            "/api/slide-templates/**"};

    public CookieBearerTokenResolver(String cookieName) {
        this.cookieName = cookieName;
        delegate.setAllowUriQueryParameter(true);
    }

    @Override
    public String resolve(HttpServletRequest request) {
        String requestPath = request.getRequestURI();
        for (String publicEndpoint : PUBLIC_ENDPOINTS) {
            if (pathMatcher.match(publicEndpoint, requestPath)) {
                return null; // No token extraction for public endpoints
            }
        }

        // First, try to get token from Authorization header (for mobile clients)
        String token = delegate.resolve(request);
        if (token != null && !token.isBlank()) {
            log.info("token is used from header");
            return token;
        }

        // Fall back to cookie (for web clients)
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if (cookieName.equals(c.getName()) && c.getValue() != null && !c.getValue().isBlank()) {
                    log.info("token is used from cookie");
                    return c.getValue();
                }
            }
        }

        return null;
    }
}
