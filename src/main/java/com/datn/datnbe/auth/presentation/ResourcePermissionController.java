package com.datn.datnbe.auth.presentation;

import com.datn.datnbe.auth.api.ResourcePermissionApi;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.datn.datnbe.auth.dto.request.ResourceRegistrationRequest;
import com.datn.datnbe.auth.dto.request.ResourceShareRequest;
import com.datn.datnbe.auth.dto.request.RevokeAccessRequest;
import com.datn.datnbe.auth.dto.response.DocumentRegistrationResponse;
import com.datn.datnbe.auth.dto.response.ResourceShareResponse;
import com.datn.datnbe.auth.dto.response.ResourcePermissionResponse;
import com.datn.datnbe.auth.dto.response.ResourceResponse;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourcePermissionController {
    private final ResourcePermissionApi resourcePermissionApi;

    @PostMapping("/register")
    public ResponseEntity<AppResponseDto<DocumentRegistrationResponse>> registerResource(
            @Valid @RequestBody ResourceRegistrationRequest request,
            Authentication authentication) {

        log.info("Registering resource {} in Keycloak", request.getId());

        // Extract user ID from JWT if ownerId not provided
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String ownerId = jwt.getSubject();

        // Register resource in Keycloak with resource type
        var response = resourcePermissionApi.registerResource(request, ownerId);
        log.info("Successfully registered resource {} with Keycloak resource ID {}",
                response.getId(),
                response.getKeycloakResourceId());

        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    @PostMapping("/{documentId}/share")
    public ResponseEntity<AppResponseDto<ResourceShareResponse>> shareResource(@PathVariable String documentId,
            @Valid @RequestBody ResourceShareRequest request,
            Authentication authentication) {

        log.info("Sharing document {} with {} users with permission {}",
                documentId,
                request.getTargetUserIds().size(),
                request.getPermission());

        // Extract current user ID from JWT
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String currentUserId = jwt.getSubject();

        // Share the resource (will look up mapping table internally)
        var response = resourcePermissionApi.shareDocument(documentId, request, currentUserId);
        log.info("Successfully shared resource {} with {} users (success: {}, failed: {})",
                documentId,
                request.getTargetUserIds().size(),
                response.getSuccessCount(),
                response.getFailedCount());

        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    @GetMapping("/{documentId}/permissions")
    public ResponseEntity<AppResponseDto<ResourcePermissionResponse>> checkPermissions(@PathVariable String documentId,
            Authentication authentication) {

        log.info("Checking permissions for document {}", documentId);

        // Extract user info and token
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String userId = jwt.getSubject();

        // Check permissions (will look up mapping table internally)
        var response = resourcePermissionApi.checkUserPermissions(documentId, userId);
        log.info("User {} has permissions {} on resource {}", userId, response.getPermissions(), documentId);

        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    @PostMapping("/{documentId}/revoke")
    public ResponseEntity<AppResponseDto<String>> revokeAccess(@PathVariable String documentId,
            @RequestBody @Valid RevokeAccessRequest request,
            Authentication authentication) {

        log.info("Revoking access to document {} from user {}", documentId, request.getTargetUserId());

        Jwt jwt = (Jwt) authentication.getPrincipal();
        String currentUserId = jwt.getSubject();

        resourcePermissionApi.revokeDocumentAccess(documentId, request.getTargetUserId(), currentUserId);

        return ResponseEntity.ok(AppResponseDto.success("Access revoked successfully"));
    }

    @GetMapping
    public ResponseEntity<AppResponseDto<List<ResourceResponse>>> getAllResources(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String currentUserId = jwt.getSubject();
        log.info("get all resource for user {}", currentUserId);
        
        var response = resourcePermissionApi.getAllResource(currentUserId);
        return ResponseEntity.ok(AppResponseDto.success(response));
    }
}
