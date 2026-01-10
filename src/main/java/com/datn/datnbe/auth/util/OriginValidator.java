package com.datn.datnbe.auth.util;

import com.datn.datnbe.auth.config.AuthProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;

/**
 * Utility component to validate request origins for admin frontend isolation.
 * Prevents privilege escalation by ensuring admin cookies are only accepted from admin origins.
 * Regular app cookies are accepted from any non-admin origin.
 *
 * Origins are configured via application.yml:
 * - app.auth.properties.admin-origins
 */
@Component
@RequiredArgsConstructor
public class OriginValidator {

    private final AuthProperties authProperties;

    /**
     * Check if the given origin matches admin frontend origins
     *
     * @param origin the origin URL to check
     * @return true if origin is from admin frontend
     */
    public boolean isAdminOrigin(String origin) {
        List<String> adminOrigins = authProperties.getAdminOrigins();
        return origin != null && adminOrigins != null
                && adminOrigins.stream().anyMatch(adminOrigin -> origin.startsWith(adminOrigin));
    }

    /**
     * Extract origin from HTTP request headers.
     * Tries Origin header first, falls back to Referer header.
     *
     * @param request the HTTP request
     * @return extracted origin URL or null if not found
     */
    public String extractOrigin(HttpServletRequest request) {
        // First try Origin header (CORS requests)
        String origin = request.getHeader("Origin");
        if (origin != null && !origin.isEmpty()) {
            return origin;
        }

        // Fallback to Referer header
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isEmpty()) {
            try {
                URI uri = URI.create(referer);
                String scheme = uri.getScheme();
                String host = uri.getHost();
                int port = uri.getPort();

                // Build origin from referer
                StringBuilder originBuilder = new StringBuilder();
                originBuilder.append(scheme).append("://").append(host);

                // Include port if not default
                if (port != -1 && port != 80 && port != 443) {
                    originBuilder.append(":").append(port);
                }

                return originBuilder.toString();
            } catch (Exception e) {
                // Invalid referer format, return null
                return null;
            }
        }

        return null;
    }
}
