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

    private static final String[] PUBLIC_ENDPOINTS = {"/public/**", "/api/auth/**", "/api/admin/auth/**",
            "/api/resources/register", "/v3/**", "/api/models", "/api/models/**", "/api/slide-themes",
            "/api/slide-themes/**", "/api/slide-templates", "/api/slide-templates/**"};

    public CookieBearerTokenResolver(String cookieName, OriginValidator originValidator) {
        this.cookieName = cookieName;
        this.originValidator = originValidator;
        delegate.setAllowUriQueryParameter(true);
    }

    @Override
    public String resolve(HttpServletRequest request) {
        String requestPath = request.getRequestURI();

        // Skip token extraction for public endpoints
        for (String publicEndpoint : PUBLIC_ENDPOINTS) {
            if (pathMatcher.match(publicEndpoint, requestPath)) {
                return null;
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
            String origin = originValidator.extractOrigin(request);

            for (Cookie c : request.getCookies()) {
                if (c.getValue() == null || c.getValue().isBlank()) {
                    continue;
                }

                // Check admin cookie - only accept from admin origins
                if ("admin_access_token".equals(c.getName())) {
                    if (originValidator.isAdminOrigin(origin)) {
                        log.info("admin token used from admin origin: {}", origin);
                        return c.getValue();
                    } else {
                        log.warn("admin token rejected from non-admin origin: {}", origin);
                        continue;
                    }
                }

                // Check app cookie - only accept from app origins
                if ("access_token".equals(c.getName())) {
                    if (originValidator.isAppOrigin(origin)) {
                        log.info("app token used from app origin: {}", origin);
                        return c.getValue();
                    } else {
                        log.warn("app token rejected from non-app origin: {}", origin);
                        continue;
                    }
                }
            }
        }

        return null;
    }
}
