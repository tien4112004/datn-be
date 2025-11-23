package com.datn.datnbe.auth.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;

@Slf4j
public class CookieBearerTokenResolver implements BearerTokenResolver {
    private final DefaultBearerTokenResolver delegate = new DefaultBearerTokenResolver();
    private final String cookieName;

    public CookieBearerTokenResolver(String cookieName) {
        this.cookieName = cookieName;
        delegate.setAllowUriQueryParameter(true);
    }

    @Override
    public String resolve(HttpServletRequest request) {
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
