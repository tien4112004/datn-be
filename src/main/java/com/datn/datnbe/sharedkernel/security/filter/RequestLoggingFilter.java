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

            // Skip body caching for multipart requests to allow Spring's multipart resolver to work
            boolean isMultipart = contentType != null && contentType.toLowerCase().contains("multipart/");

            HttpServletRequest requestToUse = request;
            if (!isMultipart) {
                // Use custom wrapper that caches body on first read (only for non-multipart)
                requestToUse = new CachedBodyHttpServletRequest(request);
            }

            long startTime = System.currentTimeMillis();

            String method = request.getMethod();
            String path = request.getRequestURI();
            String queryString = request.getQueryString();
            String origin = request.getHeader("Origin");
            String remoteAddr = getClientIp(request);

            // Log incoming request with body if applicable
            String bodyStr = "";
            if (!isMultipart && shouldLogBody(method, contentType)) {
                bodyStr = ((CachedBodyHttpServletRequest) requestToUse).getBody();
            }

            String pathWithQuery = path + (queryString != null ? "?" + queryString : "");
            String headers = getHeadersAsString(request);

            log.info(
                    ">>> INCOMING REQUEST - Method: {}, Path: {}, Origin: {}, RemoteAddr: {}, ContentType: {}, Headers: {}, Body: {}",
                    method,
                    pathWithQuery,
                    origin != null ? origin : "N/A",
                    remoteAddr,
                    contentType != null ? contentType : "N/A",
                    headers);

            try {
                filterChain.doFilter(requestToUse, response);
            } finally {
                long duration = System.currentTimeMillis() - startTime;
                int status = response.getStatus();
                String responseHeaders = getResponseHeadersAsString(response);
                log.info("<<< RESPONSE - Method: {}, Path: {}, Status: {}, Duration: {}ms, Headers: {}",
                        method,
                        pathWithQuery,
                        status,
                        duration,
                        responseHeaders);
            }
        }

        private boolean shouldLogBody(String method, String contentType) {
            return ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)
                    || "PATCH".equalsIgnoreCase(method))
                    && contentType != null
                    && (contentType.contains("application/json")
                            || contentType.contains("application/x-www-form-urlencoded")
                            || contentType.contains("text/plain"));
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

        private String getResponseHeadersAsString(HttpServletResponse response) {
            StringBuilder headers = new StringBuilder();
            var headerNames = response.getHeaderNames();

            boolean first = true;
            for (String headerName : headerNames) {
                if (!first) {
                    headers.append(", ");
                }
                String headerValue = response.getHeader(headerName);
                headers.append(headerName).append("=").append(headerValue);
                first = false;
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
            // Don't log static resources and health checks
            return path.startsWith("/static/") || path.startsWith("/resources/") || path.startsWith("/actuator/health")
                    || path.endsWith(".js") || path.endsWith(".css") || path.endsWith(".png") || path.endsWith(".jpg")
                    || path.endsWith(".gif") || path.endsWith(".ico");
        }
    }

}
