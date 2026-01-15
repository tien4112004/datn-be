package com.datn.datnbe.auth.api;

import java.util.List;

import com.datn.datnbe.auth.dto.request.PublicAccessRequest;
import com.datn.datnbe.auth.dto.request.ResourceRegistrationRequest;
import com.datn.datnbe.auth.dto.request.ResourceShareRequest;
import com.datn.datnbe.auth.dto.response.DocumentRegistrationResponse;
import com.datn.datnbe.auth.dto.response.PublicAccessResponse;
import com.datn.datnbe.auth.dto.response.ResourcePermissionResponse;
import com.datn.datnbe.auth.dto.response.ResourceResponse;
import com.datn.datnbe.auth.dto.response.ResourceShareResponse;
import com.datn.datnbe.auth.dto.response.ShareStateResponse;
import com.datn.datnbe.auth.dto.response.SharedUserResponse;

public interface ResourcePermissionApi {
    public DocumentRegistrationResponse registerResource(ResourceRegistrationRequest resourceRegistrationRequest,
            String ownerId);
    public ResourcePermissionResponse checkUserPermissions(String documentId, String userId);
    public ResourceShareResponse shareDocument(String documentId, ResourceShareRequest request, String currentUserId);
    public void revokeDocumentAccess(String documentId, String targetUserId, String currentUserId);
    public List<String> getAllResourceByTypeOfOwner(String ownerId, String resourceType);
    public List<ResourceResponse> getAllResource(String ownerId);
    public List<SharedUserResponse> getSharedUsers(String documentId, String currentUserId);
    public PublicAccessResponse setPublicAccess(String documentId, PublicAccessRequest request, String currentUserId);
    public PublicAccessResponse getPublicAccessStatus(String documentId, String currentUserId);
    public ShareStateResponse getShareState(String documentId, String currentUserId);
}
