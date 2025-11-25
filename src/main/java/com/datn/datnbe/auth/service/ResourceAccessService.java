package com.datn.datnbe.auth.service;

import com.datn.datnbe.auth.dto.keycloak.KeycloakResourceDto;
import com.datn.datnbe.auth.dto.response.ResourcePermissionResponse;
import com.datn.datnbe.auth.entity.DocumentResourceMapping;
import com.datn.datnbe.auth.mapper.ResourcePermissionMapper;
import com.datn.datnbe.auth.repository.DocumentResourceMappingRepository;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for checking and verifying resource access permissions.
 * Focused on read operations for authorization checks.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceAccessService {

    private final DocumentResourceMappingRepository mappingRepository;
    private final KeycloakAuthorizationService keycloakAuthzService;
    private final ResourcePermissionMapper mapper;

    /**
     * Check user permissions on a document resource
     *
     * @param documentId Document identifier
     * @param userToken User's JWT access token
     * @param userId User identifier
     * @return ResourcePermissionResponse with granted permissions
     */
    public ResourcePermissionResponse checkUserPermissions(String documentId, String userToken, String userId) {
        log.debug("Checking permissions for document {} by user {}", documentId, userId);

        // Look up the Keycloak resource ID from mapping table
        DocumentResourceMapping mapping = mappingRepository.findByDocumentId(documentId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Document " + documentId + " not found in Keycloak registry"));

        // Check permissions via Keycloak API using resource ID
        List<String> permissions = keycloakAuthzService.checkUserPermissions(userToken,
                mapping.getKeycloakResourceId());

        log.debug("User {} has permissions {} on document {}", userId, permissions, documentId);

        return mapper.toResourcePermissionResponse(documentId, userId, permissions);
    }

    /**
     * Verify if user is the owner of a resource
     *
     * @param documentId Document identifier
     * @param userId User identifier to check
     * @return true if user is the owner, false otherwise
     */
    public boolean isResourceOwner(String documentId, String userId) {
        DocumentResourceMapping mapping = mappingRepository.findByDocumentId(documentId)
                .orElseThrow(
                        () -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Document " + documentId + " not found"));

        KeycloakResourceDto resource = keycloakAuthzService.getResource(mapping.getKeycloakResourceId());
        boolean isOwner = resource.getOwner().equals(userId);

        log.debug("User {} is {} owner of document {}", userId, isOwner ? "the" : "not the", documentId);
        return isOwner;
    }

    /**
     * Get Keycloak resource details for a document
     *
     * @param documentId Document identifier
     * @return KeycloakResourceDto with resource details
     */
    public KeycloakResourceDto getResourceDetails(String documentId) {
        DocumentResourceMapping mapping = mappingRepository.findByDocumentId(documentId)
                .orElseThrow(
                        () -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Document " + documentId + " not found"));

        return keycloakAuthzService.getResource(mapping.getKeycloakResourceId());
    }
}
