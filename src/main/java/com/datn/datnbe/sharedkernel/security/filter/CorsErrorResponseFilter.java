package com.datn.datnbe.sharedkernel.security.filter;

import java.io.IOException;

import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * This filter is disabled as Spring Security already handles CORS properly.
 * Keeping the file for reference but not registering it as a component.
 */
@Slf4j
public class CorsErrorResponseFilter extends OncePerRequestFilter {

    private final CorsConfigurationSource corsConfigurationSource;

    public CorsErrorResponseFilter(CorsConfigurationSource corsConfigurationSource) {
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return true;
    }
}
