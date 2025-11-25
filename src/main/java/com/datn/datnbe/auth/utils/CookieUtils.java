package com.datn.datnbe.auth.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for cookie management in authentication flows
 */
@Slf4j
@UtilityClass
public class CookieUtils {
    public static final String ACCESS_TOKEN = "access_token";
    public static final String REFRESH_TOKEN = "refresh_token";
    public static final String JSESSIONID = "JSESSIONID";
    public static final int REFRESH_TOKEN_MAX_AGE = 7 * 24 * 60 * 60; // 7 days in seconds

    /**
     * Create a secure HTTP-only cookie
     *
     * @param name Cookie name
     * @param value Cookie value
     * @param maxAge Maximum age in seconds
     * @return Configured Cookie object
     */
    public static Cookie createCookie(String name, String value, int maxAge) {
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
    public static void deleteCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setPath("/"); // Must match the path used when creating the cookie
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0); // Expire immediately
        response.addCookie(cookie);

        log.debug("Deleting cookie: name={}", name);
    }

    public static String extractTokensFromCookies(HttpServletRequest request, String key) {
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
}
