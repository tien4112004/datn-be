package com.datn.datnbe.sharedkernel.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Enumeration;

@Slf4j
@Configuration
public class RequestLoggingFilter {

    @Bean
    public FilterRegistrationBean<RequestLoggingFilterImpl> loggingFilter() {
        FilterRegistrationBean<RequestLoggingFilterImpl> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new RequestLoggingFilterImpl());
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(0);
        registrationBean.setName("requestLoggingFilter");
        return registrationBean;
    }

    @Slf4j
    public static class RequestLoggingFilterImpl extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(HttpServletRequest request,
                HttpServletResponse response,
                FilterChain filterChain) throws ServletException, IOException {

            String contentType = request.getContentType();
            String method = request.getMethod();

            long startTime = System.currentTimeMillis();

            String path = request.getRequestURI();
            String queryString = request.getQueryString();
            String origin = request.getHeader("Origin");
            String remoteAddr = getClientIp(request);
            String pathWithQuery = path + (queryString != null ? "?" + queryString : "");
            getHeadersAsString(request);

            // Log incoming request
            log.info(">>> REQUEST - Method: {}, Path: {}, Origin: {}, RemoteAddr: {}, ContentType: {}",
                    method,
                    pathWithQuery,
                    origin != null ? origin : "N/A",
                    remoteAddr,
                    contentType != null ? contentType : "N/A");

            try {
                filterChain.doFilter(request, response);
            } finally {
                long duration = System.currentTimeMillis() - startTime;
                int status = response.getStatus();

                log.info("<<< RESPONSE - Method: {}, Path: {}, Status: {}, Duration: {}ms",
                        method,
                        pathWithQuery,
                        status,
                        duration);
            }
        }

        private String getClientIp(HttpServletRequest request) {
            String[] headers = {"X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP",
                    "HTTP_X_FORWARDED_FOR"};

            for (String header : headers) {
                String value = request.getHeader(header);
                if (value != null && !value.isEmpty() && !value.equalsIgnoreCase("unknown")) {
                    return value.split(",")[0].trim();
                }
            }

            return request.getRemoteAddr();
        }

        private String getHeadersAsString(HttpServletRequest request) {
            Enumeration<String> headerNames = request.getHeaderNames();
            StringBuilder headers = new StringBuilder();

            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                String headerValue = request.getHeader(headerName);

                // Hide sensitive headers
                if (isSensitiveHeader(headerName)) {
                    headerValue = "***REDACTED***";
                }

                headers.append(headerName).append("=").append(headerValue);
                if (headerNames.hasMoreElements()) {
                    headers.append(", ");
                }
            }

            return headers.toString();
        }

        private boolean isSensitiveHeader(String headerName) {
            String lowerName = headerName.toLowerCase();
            return lowerName.contains("authorization") || lowerName.contains("cookie") || lowerName.contains("password")
                    || lowerName.contains("token") || lowerName.contains("secret");
        }

        @Override
        protected boolean shouldNotFilter(HttpServletRequest request) {
            String path = request.getRequestURI();
            // Don't log static resources, health checks, and streaming endpoints
            return path.startsWith("/static/") || path.startsWith("/resources/") || path.startsWith("/actuator/health")
                    || path.endsWith(".js") || path.endsWith(".css") || path.endsWith(".png") || path.endsWith(".jpg")
                    || path.endsWith(".gif") || path.endsWith(".ico")
                    || path.equals("/api/presentations/outline-generate") || path.equals("/api/presentations/generate");
        }
    }

}
