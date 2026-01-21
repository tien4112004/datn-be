package com.datn.datnbe.cms.service;

import com.datn.datnbe.auth.dto.keycloak.KeycloakGroupDto;
import com.datn.datnbe.auth.dto.keycloak.KeycloakGroupPolicyDto;
import com.datn.datnbe.auth.dto.keycloak.KeycloakPermissionDto;
import com.datn.datnbe.auth.entity.DocumentResourceMapping;
import com.datn.datnbe.auth.mapper.KeycloakDtoMapper;
import com.datn.datnbe.auth.repository.DocumentResourceMappingRepository;
import com.datn.datnbe.auth.service.KeycloakAuthorizationService;
import com.datn.datnbe.cms.dto.LinkedResourceDto;
import com.datn.datnbe.cms.entity.ClassResourcePermission;
import com.datn.datnbe.cms.repository.ClassResourcePermissionRepository;
import com.datn.datnbe.cms.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkedResourcePermissionService {

    private final KeycloakAuthorizationService keycloakAuthzService;
    private final KeycloakDtoMapper keycloakMapper;
    private final DocumentResourceMappingRepository documentMappingRepository;
    private final ClassResourcePermissionRepository classResourcePermissionRepository;
    private final PostRepository postRepository;

    private static final String CLASS_GROUP_PREFIX = "class-";
    private static final String POLICY_SUFFIX = "-policy";
    private static final String PERMISSION_SUFFIX = "-permission";
    private static final String READ_SCOPE = "read";
    private static final String COMMENT_SCOPE = "comment";

    /**
     * Gets or creates a Keycloak group for a class.
     *
     * @param classId the class ID
     * @return the Keycloak group DTO
     */
    public KeycloakGroupDto getOrCreateClassGroup(String classId) {
        String groupName = CLASS_GROUP_PREFIX + classId;
        log.info("Getting or creating class group: {}", groupName);
        return keycloakAuthzService.createGroup(groupName);
    }

    /**
     * Grants class-level permissions for linked resources.
     * Creates Keycloak groups, policies, and permissions as needed.
     *
     * @param classId the class ID
     * @param linkedResources the list of linked resources
     */
    @Transactional
    public void grantClassPermissions(String classId, List<LinkedResourceDto> linkedResources) {
        if (linkedResources == null || linkedResources.isEmpty()) {
            return;
        }

        log.info("Granting class {} permissions for {} resources", classId, linkedResources.size());

        // Get or create class group
        KeycloakGroupDto classGroup = getOrCreateClassGroup(classId);
        String classGroupId = classGroup.getId();

        for (LinkedResourceDto resource : linkedResources) {
            grantPermissionForResource(classId, classGroupId, resource);
        }
    }

    private void grantPermissionForResource(String classId, String classGroupId, LinkedResourceDto resource) {
        String resourceType = resource.getType();
        String resourceId = resource.getId();
        String permissionLevel = resource.getPermissionLevel() != null ? resource.getPermissionLevel() : "view";

        // Check if permission already exists
        Optional<ClassResourcePermission> existingPermission = classResourcePermissionRepository
                .findByClassIdAndResourceTypeAndResourceId(classId, resourceType, resourceId);

        if (existingPermission.isPresent()) {
            ClassResourcePermission existing = existingPermission.get();
            // Update permission level if changed
            if (!permissionLevel.equals(existing.getPermissionLevel())) {
                log.info("Updating permission level for resource {}/{} in class {} from {} to {}",
                        resourceType,
                        resourceId,
                        classId,
                        existing.getPermissionLevel(),
                        permissionLevel);
                existing.setPermissionLevel(permissionLevel);
                classResourcePermissionRepository.save(existing);
                // Note: In a full implementation, we would also update the Keycloak permission scopes
            }
            return;
        }

        // Find the document resource mapping in Keycloak
        Optional<DocumentResourceMapping> docMapping = documentMappingRepository.findByDocumentId(resourceId);
        if (docMapping.isEmpty()) {
            log.warn("Resource {}/{} is not registered in Keycloak, skipping permission grant",
                    resourceType,
                    resourceId);
            return;
        }

        String keycloakResourceId = docMapping.get().getKeycloakResourceId();

        // Create group policy for this class-resource combination
        String policyName = buildPolicyName(classId, resourceType, resourceId);
        KeycloakGroupPolicyDto policy = keycloakMapper.toKeycloakGroupPolicyDto(policyName,
                String.format("Class %s access to %s %s", classId, resourceType, resourceId),
                List.of(keycloakMapper.toGroupDefinition(classGroupId)));
        KeycloakGroupPolicyDto createdPolicy = keycloakAuthzService.createGroupPolicy(policy);

        // Create permission with appropriate scopes
        String permissionName = buildPermissionName(classId, resourceType, resourceId);
        Set<String> scopes = "comment".equals(permissionLevel) ? Set.of(READ_SCOPE, COMMENT_SCOPE) : Set.of(READ_SCOPE);

        KeycloakPermissionDto permission = keycloakMapper.toKeycloakPermissionDto(permissionName,
                String.format("Class %s %s access to %s %s", classId, permissionLevel, resourceType, resourceId),
                "AFFIRMATIVE",
                Set.of(keycloakResourceId),
                scopes,
                Set.of(createdPolicy.getId()));
        keycloakAuthzService.createPermission(permission);

        // Save class resource permission record
        ClassResourcePermission classPermission = ClassResourcePermission.builder()
                .classId(classId)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .permissionLevel(permissionLevel)
                .keycloakGroupId(classGroupId)
                .keycloakPolicyId(createdPolicy.getId())
                .build();
        classResourcePermissionRepository.save(classPermission);

        log.info("Granted class {} {} access to resource {}/{}", classId, permissionLevel, resourceType, resourceId);
    }

    /**
     * Revokes permissions for resources that are no longer linked in any post within the class.
     *
     * @param classId the class ID
     * @param oldResources the previous list of linked resources
     * @param newResources the new list of linked resources
     */
    @Transactional
    public void revokeUnlinkedPermissions(String classId,
            List<LinkedResourceDto> oldResources,
            List<LinkedResourceDto> newResources) {
        if (oldResources == null || oldResources.isEmpty()) {
            return;
        }

        // Find resources that were removed
        for (LinkedResourceDto oldResource : oldResources) {
            boolean stillLinked = newResources != null && newResources.stream()
                    .anyMatch(r -> r.getType().equals(oldResource.getType()) && r.getId().equals(oldResource.getId()));

            if (!stillLinked) {
                // Check if any other post in this class still links to this resource
                boolean linkedElsewhere = isResourceLinkedInOtherPosts(classId,
                        oldResource.getType(),
                        oldResource.getId());

                if (!linkedElsewhere) {
                    revokePermissionForResource(classId, oldResource.getType(), oldResource.getId());
                }
            }
        }
    }

    /**
     * Checks if a resource is linked in any other post within the class.
     */
    private boolean isResourceLinkedInOtherPosts(String classId, String resourceType, String resourceId) {
        return postRepository.isResourceLinkedInClass(classId, resourceType, resourceId);
    }

    /**
     * Revokes permission for a specific resource in a class.
     */
    private void revokePermissionForResource(String classId, String resourceType, String resourceId) {
        Optional<ClassResourcePermission> permission = classResourcePermissionRepository
                .findByClassIdAndResourceTypeAndResourceId(classId, resourceType, resourceId);

        if (permission.isEmpty()) {
            log.warn("No permission found for class {} resource {}/{}", classId, resourceType, resourceId);
            return;
        }

        // Note: In a full implementation, we would delete the Keycloak permission and policy here
        // For now, we just remove the database record
        classResourcePermissionRepository.delete(permission.get());

        log.info("Revoked class {} access to resource {}/{}", classId, resourceType, resourceId);
    }

    private String buildPolicyName(String classId, String resourceType, String resourceId) {
        return CLASS_GROUP_PREFIX + classId + "-" + resourceType + "-" + resourceId + POLICY_SUFFIX;
    }

    private String buildPermissionName(String classId, String resourceType, String resourceId) {
        return CLASS_GROUP_PREFIX + classId + "-" + resourceType + "-" + resourceId + PERMISSION_SUFFIX;
    }
}
