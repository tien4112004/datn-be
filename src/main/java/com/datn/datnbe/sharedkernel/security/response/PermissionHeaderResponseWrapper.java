package com.datn.datnbe.sharedkernel.security.response;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerInterceptor;

import com.datn.datnbe.sharedkernel.security.aspect.DocumentPermissionAspect;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PermissionHeaderResponseWrapper implements HandlerInterceptor {

    private static final String PERMISSION_HEADER = "permission";

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            if (attributes != null) {
                Set<String> permissions = (Set<String>) attributes.getAttribute(
                        DocumentPermissionAspect.USER_PERMISSIONS_ATTRIBUTE,
                        ServletRequestAttributes.SCOPE_REQUEST);

                if (permissions != null && !permissions.isEmpty()) {
                    // Format: "read, comment, write" (sorted, comma-space separated)
                    String permissionsHeader = permissions.stream().sorted().collect(Collectors.joining(", "));
                    response.setHeader(PERMISSION_HEADER, permissionsHeader);
                    log.debug("Added permissions header to response: {}", permissionsHeader);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to add permission header to response", e);
        }
    }
}
