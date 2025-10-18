package com.datn.datnbe.sharedkernel.security.aspect;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import com.datn.datnbe.sharedkernel.security.annotation.RequireDocumentPermission;
import com.datn.datnbe.sharedkernel.security.utils.SecurityContextUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Aspect that intercepts methods annotated with @RequireDocumentPermission
 * and validates that the current user has the required permissions on the document.
 *
 * Uses reflection and ApplicationContext to dynamically load the permission service
 * to avoid circular module dependencies between sharedkernel and auth modules.
 */
@Aspect
@Component
@Order(1)
@Slf4j
public class DocumentPermissionAspect {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private SecurityContextUtils securityContextUtils;

    @Before("@annotation(requirePermission)")
    public void checkDocumentPermission(JoinPoint joinPoint, RequireDocumentPermission requirePermission) {
        log.debug("Checking document permissions for method: {}", joinPoint.getSignature().getName());

        String documentId = extractDocumentId(joinPoint, requirePermission.documentIdParam());
        if (documentId == null || documentId.isEmpty()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Document ID not found in request");
        }

        String userId = securityContextUtils.getCurrentUserId();
        String userToken = securityContextUtils.getCurrentUserToken();

        if (userId == null || userToken == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "User not authenticated");
        }

        log.debug("Checking permissions for user {} on document {}", userId, documentId);

        // Get the permission service dynamically to avoid circular dependency
        Object permissionService = applicationContext.getBean("resourcePermissionService");
        Set<String> userPermissions;

        try {
            // Call: permissionService.checkUserPermissions(documentId, userToken, userId).getPermissions()
            var checkMethod = permissionService.getClass()
                    .getMethod("checkUserPermissions", String.class, String.class, String.class);
            var response = checkMethod.invoke(permissionService, documentId, userToken, userId);
            var getPermissionsMethod = response.getClass().getMethod("getPermissions");
            @SuppressWarnings("unchecked") Set<String> permissions = (Set<String>) getPermissionsMethod
                    .invoke(response);
            userPermissions = permissions;
        } catch (Exception e) {
            // Unwrap reflection exceptions to get the actual cause
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            if (cause instanceof AppException appEx) {
                if (appEx.getErrorCode() == ErrorCode.RESOURCE_NOT_FOUND) {
                    throw new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                            "Document not found or not registered for permission control");
                }
                throw appEx;
            }
            log.error("Failed to check permissions", e);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "Failed to check permissions: " + (cause != null ? cause.getMessage() : "Unknown error"));
        }

        List<String> requiredScopes = Arrays.asList(requirePermission.scopes());
        boolean hasAllPermissions = requiredScopes.stream().allMatch(userPermissions::contains);

        if (!hasAllPermissions) {
            log.warn("User {} lacks required permissions {} on document {}. User has: {}",
                    userId,
                    requiredScopes,
                    documentId,
                    userPermissions);

            if (requirePermission.throwOnFail()) {
                throw new AppException(ErrorCode.FORBIDDEN,
                        "You don't have sufficient permissions to access this document");
            }
        }

        log.debug("Permission check passed for user {} on document {}", userId, documentId);
    }

    /**
     * Extract document ID from method parameters based on annotation configuration
     */
    private String extractDocumentId(JoinPoint joinPoint, String paramName) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        java.lang.reflect.Parameter[] parameters = signature.getMethod().getParameters();

        for (int i = 0; i < parameterNames.length; i++) {
            // Check if parameter name matches
            if (parameterNames[i].equals(paramName)) {
                return String.valueOf(args[i]);
            }

            // Check if parameter has @PathVariable or @RequestParam annotation with matching value
            if (parameters[i].isAnnotationPresent(PathVariable.class)) {
                PathVariable pathVar = parameters[i].getAnnotation(PathVariable.class);
                String pathVarName = pathVar.value().isEmpty() ? pathVar.name() : pathVar.value();
                if (pathVarName.isEmpty()) {
                    pathVarName = parameterNames[i];
                }
                if (pathVarName.equals(paramName)) {
                    return String.valueOf(args[i]);
                }
            }

            if (parameters[i].isAnnotationPresent(RequestParam.class)) {
                RequestParam reqParam = parameters[i].getAnnotation(RequestParam.class);
                String reqParamName = reqParam.value().isEmpty() ? reqParam.name() : reqParam.value();
                if (reqParamName.isEmpty()) {
                    reqParamName = parameterNames[i];
                }
                if (reqParamName.equals(paramName)) {
                    return String.valueOf(args[i]);
                }
            }
        }

        log.error("Could not find document ID parameter '{}' in method {}", paramName, signature.getName());
        return null;
    }
}
