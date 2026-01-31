package com.datn.datnbe.sharedkernel.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

            // Read and cache the request body BEFORE wrapping
            // Use array to allow mutation while being effectively final
            final byte[][] bodyBytesHolder = new byte[1][];
            bodyBytesHolder[0] = new byte[0];
            String requestBodyStr = "N/A";
            
            if (needsRequestBodyLogging) {
                try {
                    // Read the body from the original request
                    InputStream inputStream = request.getInputStream();
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    byte[] data = new byte[MAX_BODY_CACHE_SIZE];
                    int nRead;
                    while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                        buffer.write(data, 0, nRead);
                        if (buffer.size() > MAX_BODY_CACHE_SIZE) {
                            break;
                        }
                    }
                    bodyBytesHolder[0] = buffer.toByteArray();
                    
                    // Convert to string for logging
                    if (bodyBytesHolder[0].length > 0) {
                        int length = Math.min(bodyBytesHolder[0].length, MAX_BODY_CACHE_SIZE);
                        requestBodyStr = new String(bodyBytesHolder[0], 0, length, StandardCharsets.UTF_8);
                        if (bodyBytesHolder[0].length > MAX_BODY_CACHE_SIZE) {
                            requestBodyStr += "... [TRUNCATED, total size: " + bodyBytesHolder[0].length + " bytes]";
                        }
                    }
                } catch (Exception e) {
                    requestBodyStr = "[Error reading body: " + e.getMessage() + "]";
                }
            }

            // Create wrapper with a fresh input stream from the cached body
            HttpServletRequest requestToUse = request;
            ContentCachingRequestWrapper wrappedRequest = null;
            
            if (needsRequestBodyLogging && bodyBytesHolder[0].length > 0) {
                // Create a content caching wrapper that will work with the cached body
                wrappedRequest = new ContentCachingRequestWrapper(request, MAX_BODY_CACHE_SIZE) {
                    private boolean inputStreamRead = false;

                    @Override
                    public ServletInputStream getInputStream() throws IOException {
                        if (!inputStreamRead) {
                            inputStreamRead = true;
                            final ByteArrayInputStream bais = new ByteArrayInputStream(bodyBytesHolder[0]);
                            return new ServletInputStream() {
                                @Override
                                public int read() throws IOException {
                                    return bais.read();
                                }

                                @Override
                                public boolean isFinished() {
                                    return bais.available() == 0;
                                }

                                @Override
                                public boolean isReady() {
                                    return true;
                                }

                                @Override
                                public void setReadListener(jakarta.servlet.ReadListener listener) {
                                    // Not implemented for cached input
                                }
                            };
                        }
                        return super.getInputStream();
                    }
                };
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

            // Log incoming request with body
            log.info(
                    ">>> REQUEST - Method: {}, Path: {}, Origin: {}, RemoteAddr: {}, ContentType: {}, Headers: {}, Body: {}",
                    method,
                    pathWithQuery,
                    origin != null ? origin : "N/A",
                    remoteAddr,
                    contentType != null ? contentType : "N/A",
                    headers,
                    requestBodyStr);            try {
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
