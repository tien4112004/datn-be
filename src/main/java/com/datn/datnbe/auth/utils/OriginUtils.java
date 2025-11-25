package com.datn.datnbe.auth.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;

@Slf4j
@UtilityClass
public class OriginUtils {

    public static String extractFeOrigin(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isEmpty()) {
            try {
                // Extract origin from referer URL (e.g., "http://localhost:3000/..." -> "http://localhost:3000")
                URI uri = java.net.URI.create(referer);
                String scheme = uri.getScheme() != null ? uri.getScheme() : "";
                String origin = scheme + "://" + uri.getHost();

                if ((scheme.equals("http") && uri.getPort() != 80 && uri.getPort() != -1)
                        || (scheme.equals("https") && uri.getPort() != 443 && uri.getPort() != -1)) {
                    origin += ":" + uri.getPort();
                }
                log.debug("Extracted frontend origin from Referer: {}", origin);
                return origin;
            } catch (Exception e) {
                log.warn("Failed to parse Referer header: {}", e.getMessage());
            }
        }
        // Fallback to request origin if Referer is not available
        return getOriginUrl(request);
    }

    public static String getOriginUrl(HttpServletRequest request) {
        String scheme = request.getHeader("X-Forwarded-Proto");
        if (scheme == null || scheme.isEmpty()) {
            scheme = request.getScheme();
        }

        String host = request.getHeader("X-Forwarded-Host");
        if (host == null || host.isEmpty()) {
            host = request.getHeader("Host");
        }

        if (host == null || host.isEmpty()) {
            host = request.getServerName();
            int port = request.getServerPort();
            if ((scheme.equals("http") && port != 80) || (scheme.equals("https") && port != 443)) {
                host = host + ":" + port;
            }
        }

        log.debug("Extracted origin URL - scheme: {}, host: {}", scheme, host);
        return scheme + "://" + host;
    }
}
