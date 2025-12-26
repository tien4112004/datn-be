package com.datn.datnbe.sharedkernel.security.response;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.datn.datnbe.sharedkernel.security.aspect.DocumentPermissionAspect;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import lombok.extern.slf4j.Slf4j;

/**
 * Servlet Filter that adds the permission header to the response.
 * The header is added when getOutputStream() or getWriter() is called,
 * which happens before the response body is written.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
@Slf4j
public class PermissionHeaderResponseWrapper implements Filter {

    private static final String PERMISSION_HEADER = "permission";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletResponse httpResponse = (HttpServletResponse) response;
        PermissionResponseWrapper responseWrapper = new PermissionResponseWrapper(httpResponse);

        chain.doFilter(request, responseWrapper);
    }

    private static class PermissionResponseWrapper extends HttpServletResponseWrapper {

        private boolean permissionHeaderAdded = false;

        public PermissionResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        private void addPermissionHeaderIfNeeded() {
            if (permissionHeaderAdded || isCommitted()) {
                return;
            }

            try {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                        .getRequestAttributes();
                if (attributes != null) {
                    @SuppressWarnings("unchecked") Set<String> permissions = (Set<String>) attributes.getAttribute(
                            DocumentPermissionAspect.USER_PERMISSIONS_ATTRIBUTE,
                            ServletRequestAttributes.SCOPE_REQUEST);

                    if (permissions != null && !permissions.isEmpty()) {
                        String permissionsHeader = permissions.stream().sorted().collect(Collectors.joining(", "));
                        super.setHeader(PERMISSION_HEADER, permissionsHeader);
                        permissionHeaderAdded = true;
                        log.info("Added permission header: {}", permissionsHeader);
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to add permission header", e);
            }
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            addPermissionHeaderIfNeeded();
            return super.getOutputStream();
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            addPermissionHeaderIfNeeded();
            return super.getWriter();
        }

        @Override
        public void flushBuffer() throws IOException {
            addPermissionHeaderIfNeeded();
            super.flushBuffer();
        }
    }
}
