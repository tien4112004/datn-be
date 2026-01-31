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
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

@Slf4j
@Configuration
public class RequestLoggingFilter {

    // Maximum body size to cache for logging (10KB) - prevents memory issues with large payloads
    private static final int MAX_BODY_CACHE_SIZE = 10 * 1024;

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

            // Skip body caching for multipart requests to allow Spring's multipart resolver to work
            boolean isMultipart = contentType != null && contentType.toLowerCase().contains("multipart/");
            
            // Only wrap request when we actually need to log the body
            boolean needsRequestBodyLogging = !isMultipart && shouldLogRequestBody(method, contentType);

            HttpServletRequest requestToUse = request;
            ContentCachingRequestWrapper wrappedRequest = null;
            
            if (needsRequestBodyLogging) {
                // Use Spring's ContentCachingRequestWrapper with size limit
                wrappedRequest = new ContentCachingRequestWrapper(request, MAX_BODY_CACHE_SIZE);
                requestToUse = wrappedRequest;
            }
            
            // Wrap response to capture response body
            ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

            long startTime = System.currentTimeMillis();

            String path = request.getRequestURI();
            String queryString = request.getQueryString();
            String origin = request.getHeader("Origin");
            String remoteAddr = getClientIp(request);
            String pathWithQuery = path + (queryString != null ? "?" + queryString : "");
            String headers = getHeadersAsString(request);

            // Read request body for logging (with size limit)
            String requestBodyStr = "N/A";
            if (wrappedRequest != null) {
                // Force reading the input stream to cache it
                byte[] content = wrappedRequest.getContentAsByteArray();
                if (content.length == 0) {
                    // ContentCachingRequestWrapper only caches after getInputStream() is read
                    // We need to read it manually for logging before controller processes it
                    try {
                        byte[] buffer = new byte[MAX_BODY_CACHE_SIZE];
                        int bytesRead = wrappedRequest.getInputStream().read(buffer);
                        if (bytesRead > 0) {
                            requestBodyStr = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
                            if (bytesRead == MAX_BODY_CACHE_SIZE) {
                                requestBodyStr += "... [TRUNCATED]";
                            }
                        }
                    } catch (Exception e) {
                        requestBodyStr = "[Error reading body: " + e.getMessage() + "]";
                    }
                } else {
                    int length = Math.min(content.length, MAX_BODY_CACHE_SIZE);
                    requestBodyStr = new String(content, 0, length, StandardCharsets.UTF_8);
                    if (content.length > MAX_BODY_CACHE_SIZE) {
                        requestBodyStr += "... [TRUNCATED, total size: " + content.length + " bytes]";
                    }
                }
            }

            // Log incoming request with body
            log.info(
                    ">>> REQUEST - Method: {}, Path: {}, Origin: {}, RemoteAddr: {}, ContentType: {}, Headers: {}, Body: {}",
                    method,
                    pathWithQuery,
                    origin != null ? origin : "N/A",
                    remoteAddr,
                    contentType != null ? contentType : "N/A",
                    headers,
                    requestBodyStr);

            try {
                filterChain.doFilter(requestToUse, wrappedResponse);
            } finally {
                long duration = System.currentTimeMillis() - startTime;
                int status = wrappedResponse.getStatus();
                String responseHeaders = getResponseHeadersAsString(wrappedResponse);
                
                // Get response body for logging
                String responseBodyStr = "N/A";
                String responseContentType = wrappedResponse.getContentType();
                if (shouldLogResponseBody(responseContentType)) {
                    byte[] content = wrappedResponse.getContentAsByteArray();
                    if (content.length > 0) {
                        int length = Math.min(content.length, MAX_BODY_CACHE_SIZE);
                        responseBodyStr = new String(content, 0, length, StandardCharsets.UTF_8);
                        if (content.length > MAX_BODY_CACHE_SIZE) {
                            responseBodyStr += "... [TRUNCATED, total size: " + content.length + " bytes]";
                        }
                    }
                }
                
                log.info("<<< RESPONSE - Method: {}, Path: {}, Status: {}, Duration: {}ms, Headers: {}, Body: {}",
                        method,
                        pathWithQuery,
                        status,
                        duration,
                        responseHeaders,
                        responseBodyStr);
                
                // IMPORTANT: Copy cached content to the actual response
                wrappedResponse.copyBodyToResponse();
            }
        }

        private boolean shouldLogRequestBody(String method, String contentType) {
            return ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)
                    || "PATCH".equalsIgnoreCase(method))
                    && contentType != null
                    && (contentType.contains("application/json")
                            || contentType.contains("application/x-www-form-urlencoded")
                            || contentType.contains("text/plain"));
        }
        
        private boolean shouldLogResponseBody(String contentType) {
            return contentType != null
                    && (contentType.contains("application/json")
                            || contentType.contains("text/plain")
                            || contentType.contains("text/html"));
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
