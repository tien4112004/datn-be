package com.datn.datnbe.auth.service;

import com.datn.datnbe.auth.dto.response.SignInResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;

/**
 * Service for managing user sessions and authentication cookies.
 * Consolidates cookie management and origin extraction logic.
 */
@Service
@Slf4j
public class SessionManagementService {

    private static final String ACCESS_TOKEN = "access_token";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String JSESSIONID = "JSESSIONID";
    private static final int REFRESH_TOKEN_MAX_AGE = 7 * 24 * 60 * 60; // 7 days in seconds

    /**
     * Create authenticated session with secure cookies
     *
     * @param response HTTP response to add cookies to
     * @param signInResponse Sign-in response containing tokens
     */
    public void createSession(HttpServletResponse response, SignInResponse signInResponse) {
        Cookie accessTokenCookie = createSecureCookie(ACCESS_TOKEN,
                signInResponse.getAccessToken(),
                signInResponse.getExpiresIn());
        Cookie refreshTokenCookie = createSecureCookie(REFRESH_TOKEN,
                signInResponse.getRefreshToken(),
                REFRESH_TOKEN_MAX_AGE);

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);
        log.debug("Created authenticated session for user");
    }

    /**
     * Clear session and remove all authentication cookies
     *
     * @param response HTTP response to clear cookies from
     */
    public void clearSession(HttpServletResponse response) {
        deleteCookie(response, ACCESS_TOKEN);
        deleteCookie(response, REFRESH_TOKEN);
        deleteCookie(response, JSESSIONID);
        log.debug("Cleared authenticated session");
    }

    /**
     * Extract refresh token from request cookies
     *
     * @param request HTTP request containing cookies
     * @return Refresh token value or null if not found
     */
    public String extractRefreshToken(HttpServletRequest request) {
        return extractTokenFromCookies(request, REFRESH_TOKEN);
    }

    /**
     * Extract frontend origin from HTTP request headers
     * Tries multiple sources: Origin header, Referer header, then constructs from request
     *
     * @param request HTTP request
     * @return Frontend origin URL
     */
    public String extractFrontendOrigin(HttpServletRequest request) {
        // First try Origin header
        String origin = request.getHeader("Origin");
        if (origin != null && !origin.isEmpty()) {
            log.debug("Extracted frontend origin from Origin header: {}", origin);
            return origin;
        }

        // Then try Referer header
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isEmpty()) {
            try {
                URI uri = URI.create(referer);
                String scheme = uri.getScheme() != null ? uri.getScheme() : "";
                String extractedOrigin = scheme + "://" + uri.getHost();

                if ((scheme.equals("http") && uri.getPort() != 80 && uri.getPort() != -1)
                        || (scheme.equals("https") && uri.getPort() != 443 && uri.getPort() != -1)) {
                    extractedOrigin += ":" + uri.getPort();
                }

                log.debug("Extracted frontend origin from Referer header: {}", extractedOrigin);
                return extractedOrigin;
            } catch (Exception e) {
                log.warn("Failed to parse Referer header: {}", referer, e);
            }
        }

        // Fallback to constructing from request
        return buildOriginFromRequest(request);
    }

    /**
     * Create a secure HTTP-only cookie
     *
     * @param name Cookie name
     * @param value Cookie value
     * @param maxAge Maximum age in seconds
     * @return Configured Cookie object
     */
    private Cookie createSecureCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        cookie.setSecure(true);
        cookie.setAttribute("sameSite", "None");
        return cookie;
    }

    /**
     * Delete a cookie by setting its max age to 0
     *
     * @param response HTTP response object
     * @param name Name of the cookie to delete
     */
    private void deleteCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        log.trace("Deleted cookie: {}", name);
    }

    /**
     * Extract token value from request cookies
     *
     * @param request HTTP request containing cookies
     * @param key Cookie name to extract
     * @return Token value or null if not found
     */
    private String extractTokenFromCookies(HttpServletRequest request, String key) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (key.equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isEmpty()) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Build origin URL from HTTP request properties
     *
     * @param request HTTP request
     * @return Constructed origin URL
     */
    private String buildOriginFromRequest(HttpServletRequest request) {
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

        String origin = scheme + "://" + host;
        log.debug("Built origin URL from request: {}", origin);
        return origin;
    }
}
