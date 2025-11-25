package com.datn.datnbe.auth.api;

import com.datn.datnbe.auth.dto.request.ResourceRegistrationRequest;
import com.datn.datnbe.auth.dto.request.ResourceShareRequest;
import com.datn.datnbe.auth.dto.response.DocumentRegistrationResponse;
import com.datn.datnbe.auth.dto.response.ResourcePermissionResponse;
import com.datn.datnbe.auth.dto.response.ResourceShareResponse;

public interface ResourcePermissionApi {
    public DocumentRegistrationResponse registerResource(ResourceRegistrationRequest resourceRegistrationRequest,
            String ownerId);
    public ResourcePermissionResponse checkUserPermissions(String documentId, String userToken, String userId);
    public ResourceShareResponse shareDocument(String documentId, ResourceShareRequest request, String currentUserId);
    public void revokeDocumentAccess(String documentId, String targetUserId, String currentUserId);
}
