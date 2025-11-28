package com.datn.datnbe.sharedkernel.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ReadListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.*;
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

            // Use custom wrapper that caches body on first read
            CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(request);

            long startTime = System.currentTimeMillis();

            String method = request.getMethod();
            String path = request.getRequestURI();
            String queryString = request.getQueryString();
            String origin = request.getHeader("Origin");
            String contentType = request.getContentType();
            String remoteAddr = getClientIp(request);

            // Log incoming request with body if applicable
            String bodyStr = "";
            if (shouldLogBody(method, contentType)) {
                bodyStr = wrappedRequest.getBody();
            }

            String pathWithQuery = path + (queryString != null ? "?" + queryString : "");
            String headers = getHeadersAsString(request);

            if (!bodyStr.isEmpty()) {
                log.info(
                        ">>> INCOMING REQUEST - Method: {}, Path: {}, Origin: {}, RemoteAddr: {}, ContentType: {}, Headers: {}, Body: {}",
                        method,
                        pathWithQuery,
                        origin != null ? origin : "N/A",
                        remoteAddr,
                        contentType != null ? contentType : "N/A",
                        headers,
                        bodyStr);
            } else {
                log.info(
                        ">>> INCOMING REQUEST - Method: {}, Path: {}, Origin: {}, RemoteAddr: {}, ContentType: {}, Headers: {}",
                        method,
                        pathWithQuery,
                        origin != null ? origin : "N/A",
                        remoteAddr,
                        contentType != null ? contentType : "N/A",
                        headers);
            }

            try {
                filterChain.doFilter(wrappedRequest, response);
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

    /**
     * Custom request wrapper that caches the body immediately when the wrapper is created
     */
    private static class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {
        private final byte[] cachedBody;

        public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
            super(request);
            // Read and cache the body immediately
            //            InputStream requestInputStream = request.getInputStream();
            //            this.cachedBody = requestInputStream.readAllBytes();
            cachedBody = null;
        }

        @Override
        public ServletInputStream getInputStream() {
            return new CachedBodyServletInputStream(this.cachedBody);
        }

        @Override
        public BufferedReader getReader() {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.cachedBody);
            return new BufferedReader(new InputStreamReader(byteArrayInputStream));
        }

        public String getBody() {
            try {
                String charset = this.getCharacterEncoding() != null ? this.getCharacterEncoding() : "UTF-8";
                String bodyStr = new String(this.cachedBody, charset);

                // Remove newlines and extra whitespace for inline logging
                bodyStr = bodyStr.replaceAll("\\s+", " ").trim();

                // Limit body size for logging
                if (bodyStr.length() > 1000) {
                    return bodyStr.substring(0, 1000) + "... [truncated]";
                }
                return bodyStr;
            } catch (UnsupportedEncodingException e) {
                return "[Unable to decode body]";
            }
        }
    }

    /**
     * Custom ServletInputStream that reads from cached byte array
     */
    private static class CachedBodyServletInputStream extends ServletInputStream {
        private final ByteArrayInputStream byteArrayInputStream;

        public CachedBodyServletInputStream(byte[] cachedBody) {
            this.byteArrayInputStream = new ByteArrayInputStream(cachedBody);
        }

        @Override
        public boolean isFinished() {
            return byteArrayInputStream.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int read() {
            return byteArrayInputStream.read();
        }
    }
}
