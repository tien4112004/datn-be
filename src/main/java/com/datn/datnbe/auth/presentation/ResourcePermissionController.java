package com.datn.datnbe.auth.presentation;

import com.datn.datnbe.auth.api.ResourcePermissionApi;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.datn.datnbe.auth.dto.request.PublicAccessRequest;
import com.datn.datnbe.auth.dto.request.ResourceRegistrationRequest;
import com.datn.datnbe.auth.dto.request.ResourceShareRequest;
import com.datn.datnbe.auth.dto.request.RevokeAccessRequest;
import com.datn.datnbe.auth.dto.response.DocumentRegistrationResponse;
import com.datn.datnbe.auth.dto.response.PublicAccessResponse;
import com.datn.datnbe.auth.dto.response.ResourceShareResponse;
import com.datn.datnbe.auth.dto.response.ResourcePermissionResponse;
import com.datn.datnbe.auth.dto.response.ResourceResponse;
import com.datn.datnbe.auth.dto.response.ShareStateResponse;
import com.datn.datnbe.auth.dto.response.SharedResourceResponse;
import com.datn.datnbe.auth.dto.response.SharedUserResponse;
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

    @GetMapping("/{documentId}/shared-users")
    public ResponseEntity<AppResponseDto<List<SharedUserResponse>>> getSharedUsers(@PathVariable String documentId,
            Authentication authentication) {

        log.info("Getting shared users for document {}", documentId);

        Jwt jwt = (Jwt) authentication.getPrincipal();
        String currentUserId = jwt.getSubject();

        var response = resourcePermissionApi.getSharedUsers(documentId, currentUserId);
        log.info("Found {} shared users for document {}", response.size(), documentId);

        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    @PutMapping("/{documentId}/public-access")
    public ResponseEntity<AppResponseDto<PublicAccessResponse>> setPublicAccess(@PathVariable String documentId,
            @Valid @RequestBody PublicAccessRequest request,
            Authentication authentication) {

        log.info("Setting public access for document {} - isPublic: {}, permission: {}",
                documentId,
                request.getIsPublic(),
                request.getPublicPermission());

        Jwt jwt = (Jwt) authentication.getPrincipal();
        String currentUserId = jwt.getSubject();

        var response = resourcePermissionApi.setPublicAccess(documentId, request, currentUserId);
        log.info("Successfully updated public access for document {}", documentId);

        return ResponseEntity.ok(AppResponseDto.<PublicAccessResponse>builder()
                .data(response)
                .message("Public access updated successfully")
                .build());
    }

    @GetMapping("/{documentId}/public-access")
    public ResponseEntity<AppResponseDto<PublicAccessResponse>> getPublicAccessStatus(@PathVariable String documentId,
            Authentication authentication) {

        log.info("Getting public access status for document {}", documentId);

        Jwt jwt = (Jwt) authentication.getPrincipal();
        String currentUserId = jwt.getSubject();

        var response = resourcePermissionApi.getPublicAccessStatus(documentId, currentUserId);
        log.info("Retrieved public access status for document {}: isPublic={}", documentId, response.getIsPublic());

        return ResponseEntity.ok(AppResponseDto.<PublicAccessResponse>builder()
                .data(response)
                .message("Public access status retrieved")
                .build());
    }

    @GetMapping("/{documentId}/share-state")
    public ResponseEntity<AppResponseDto<ShareStateResponse>> getShareState(@PathVariable String documentId,
            Authentication authentication) {

        log.info("Getting complete share state for document {}", documentId);

        Jwt jwt = (Jwt) authentication.getPrincipal();
        String currentUserId = jwt.getSubject();

        var response = resourcePermissionApi.getShareState(documentId, currentUserId);
        log.info("Retrieved share state for document {}: {} shared users, isPublic={}, current user permission={}",
                documentId,
                response.getSharedUsers().size(),
                response.getPublicAccess().getIsPublic(),
                response.getCurrentUserPermission());

        return ResponseEntity.ok(AppResponseDto.<ShareStateResponse>builder()
                .data(response)
                .message("Share state retrieved successfully")
                .build());
    }

    @GetMapping("/shared-with-me")
    public ResponseEntity<AppResponseDto<List<SharedResourceResponse>>> getSharedWithMe(Authentication authentication) {

        Jwt jwt = (Jwt) authentication.getPrincipal();
        String userId = jwt.getSubject();

        log.info("Getting resources shared with user {}", userId);

        var response = resourcePermissionApi.getSharedWithMe(userId);
        log.info("Found {} resources shared with user {}", response.size(), userId);

        return ResponseEntity.ok(AppResponseDto.<List<SharedResourceResponse>>builder()
                .data(response)
                .message("Shared resources retrieved successfully")
                .build());
    }
}
