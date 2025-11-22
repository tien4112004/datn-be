package com.datn.datnbe.auth.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for cookie management in authentication flows
 */
@Slf4j
@UtilityClass
public class CookieUtils {

    /**
     * Create a secure HTTP-only cookie
     *
     * @param name Cookie name
     * @param value Cookie value
     * @param maxAge Maximum age in seconds
     * @return Configured Cookie object
     */
    public static Cookie createCookie(String name, String value, int maxAge) {
        //TODO: when going production, set Secure to true
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        cookie.setAttribute("SameSite", "Lax");

        log.info("Creating cookie: name={}, maxAge={}, httpOnly=true, sameSite=Lax", name, maxAge);

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
}
