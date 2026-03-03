package com.datn.datnbe.auth.config;

import com.datn.datnbe.auth.util.OriginValidator;
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
    private final OriginValidator originValidator;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // Endpoints that are fully public (all HTTP methods)
    private static final String[] PUBLIC_ENDPOINTS = {"/public/**", "/api/auth/**", "/api/admin/auth/**",
            "/api/resources/register", "/v3/**"};

    // Endpoints that are only public for GET requests (non-GET requires authentication)
    private static final String[] PUBLIC_GET_ONLY_ENDPOINTS = {"/api/models", "/api/models/**", "/api/slide-themes",
            "/api/slide-themes/**", "/api/slide-templates", "/api/slide-templates/**"};

    public CookieBearerTokenResolver(String cookieName, OriginValidator originValidator) {
        this.cookieName = cookieName;
        this.originValidator = originValidator;
        delegate.setAllowUriQueryParameter(true);
    }

    @Override
    public String resolve(HttpServletRequest request) {
        String requestPath = request.getRequestURI();
        String method = request.getMethod();

        // Skip token extraction for fully public endpoints
        for (String publicEndpoint : PUBLIC_ENDPOINTS) {
            if (pathMatcher.match(publicEndpoint, requestPath)) {
                return null;
            }
        }

        // Skip token extraction only for GET requests on GET-only public endpoints
        if ("GET".equalsIgnoreCase(method)) {
            for (String publicGetEndpoint : PUBLIC_GET_ONLY_ENDPOINTS) {
                if (pathMatcher.match(publicGetEndpoint, requestPath)) {
                    return null;
                }
            }
        }

        // First, try Authorization header (for mobile clients)
        String token = delegate.resolve(request);
        if (token != null && !token.isBlank()) {
            log.info("token is used from header");
            return token;
        }

        // Fall back to cookies with origin validation
        if (request.getCookies() != null) {
            log.info("Token resolution for {}: {}", request.getRequestURI(), token != null ? "FOUND" : "NOT FOUND");
            String origin = originValidator.extractOrigin(request);
            for (Cookie c : request.getCookies()) {
                if (c.getValue() == null || c.getValue().isBlank()) {
                    continue;
                }

                // Check admin cookie - STRICT: only accept from admin origins
                if ("admin_access_token".equals(c.getName())) {
                    if (originValidator.isAdminOrigin(origin)) {
                        log.info("admin token used from admin origin: {}", origin);
                        return c.getValue();
                    } else {
                        log.warn("admin token rejected from non-admin origin: {}", origin);
                        continue;
                    }
                }

                // Check app cookie - RELAXED: accept unless from admin origin (prevents cross-contamination)
                if ("access_token".equals(c.getName())) {
                    if (originValidator.isAdminOrigin(origin)) {
                        log.warn("app token rejected from admin origin: {}", origin);
                        continue;
                    }
                    // Accept from any non-admin origin (including Postman, mobile apps, etc.)
                    log.info("app token used from origin: {}", origin);
                    return c.getValue();
                }
            }
        }

        return null;
    }
}
