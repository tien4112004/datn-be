package com.datn.datnbe.auth.presentation;

import java.util.HashSet;
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

import com.datn.datnbe.auth.dto.request.FileRegistrationRequest;
import com.datn.datnbe.auth.dto.request.FileShareRequest;
import com.datn.datnbe.auth.dto.request.RevokeAccessRequest;
import com.datn.datnbe.auth.dto.response.FileRegistrationResponse;
import com.datn.datnbe.auth.dto.response.FileShareResponse;
import com.datn.datnbe.auth.dto.response.ResourcePermissionResponse;
import com.datn.datnbe.auth.entity.FileResourceMapping;
import com.datn.datnbe.auth.service.FilePermissionService;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FilePermissionController {
    private final FilePermissionService filePermissionService;

    @PostMapping("/register")
    public ResponseEntity<AppResponseDto<FileRegistrationResponse>> registerFile(
            @Valid @RequestBody FileRegistrationRequest request,
            Authentication authentication) {

        log.info("Registering file {} in Keycloak", request.getFileId());

        // Extract user ID from JWT if ownerId not provided
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String ownerId = request.getOwnerId() != null ? request.getOwnerId() : jwt.getSubject();

        // Register file in Keycloak with resource type
        FileResourceMapping mapping = filePermissionService
                .registerFile(request.getFileId(), request.getFileName(), ownerId, request.getResourceType());

        // Build response
        FileRegistrationResponse response = FileRegistrationResponse.builder()
                .fileId(mapping.getFileId())
                .fileName(request.getFileName())
                .keycloakResourceId(mapping.getKeycloakResourceId())
                .ownerId(ownerId)
                .message("Successfully registered file in Keycloak")
                .success(true)
                .build();

        log.info("Successfully registered file {} with Keycloak resource ID {}",
                request.getFileId(),
                mapping.getKeycloakResourceId());

        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    @PostMapping("/{fileId}/share")
    public ResponseEntity<AppResponseDto<FileShareResponse>> shareFile(@PathVariable String fileId,
            @Valid @RequestBody FileShareRequest request,
            Authentication authentication) {

        log.info("Sharing file {} with user {}", fileId, request.getTargetUserId());

        // Extract current user ID from JWT
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String currentUserId = jwt.getSubject();

        // Share the file (will look up mapping table internally)
        filePermissionService.shareFile(fileId, request.getTargetUserId(), request.getPermissions(), currentUserId);

        // Build response
        FileShareResponse response = FileShareResponse.builder()
                .fileId(fileId)
                .sharedWithUserId(request.getTargetUserId())
                .sharedWithUsername(request.getTargetUserId()) // Using userId as username was removed
                .grantedPermissions(request.getPermissions())
                .message(
                        "Successfully shared file with " + String.join(", ", request.getPermissions()) + " permissions")
                .success(true)
                .build();

        log.info("Successfully shared file {} with user {}", fileId, request.getTargetUserId());

        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    @GetMapping("/{fileId}/permissions")
    public ResponseEntity<AppResponseDto<ResourcePermissionResponse>> checkPermissions(@PathVariable String fileId,
            Authentication authentication) {

        log.info("Checking permissions for file {}", fileId);

        // Extract user info and token
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String userId = jwt.getSubject();
        String token = jwt.getTokenValue();

        // Check permissions (will look up mapping table internally)
        List<String> permissions = filePermissionService.checkUserPermissions(fileId, token);

        // Build response
        ResourcePermissionResponse response = ResourcePermissionResponse.builder()
                .resourceId(fileId)
                .userId(userId)
                .permissions(new HashSet<>(permissions))
                .hasAccess(!permissions.isEmpty())
                .build();

        log.info("User {} has permissions {} on file {}", userId, permissions, fileId);

        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    @PostMapping("/{fileId}/cleanup-legacy-permissions")
    public ResponseEntity<AppResponseDto<String>> cleanupLegacyPermissions(@PathVariable String fileId,
            Authentication authentication) {

        log.info("Cleaning up legacy permissions for file {}", fileId);

        Jwt jwt = (Jwt) authentication.getPrincipal();
        String userId = jwt.getSubject();

        filePermissionService.cleanupLegacyPermissions(fileId, userId);

        return ResponseEntity.ok(AppResponseDto.success("Legacy permissions cleaned up successfully"));
    }

    @PostMapping("/{fileId}/revoke")
    public ResponseEntity<AppResponseDto<String>> revokeAccess(@PathVariable String fileId,
            @RequestBody @Valid RevokeAccessRequest request,
            Authentication authentication) {

        log.info("Revoking access to file {} from user {}", fileId, request.getTargetUserId());

        Jwt jwt = (Jwt) authentication.getPrincipal();
        String currentUserId = jwt.getSubject();

        filePermissionService.revokeFileAccess(fileId, request.getTargetUserId(), currentUserId);

        return ResponseEntity.ok(AppResponseDto.success("Access revoked successfully"));
    }
}
