package com.datn.datnbe.cms.service;

import com.datn.datnbe.auth.dto.keycloak.KeycloakGroupDto;
import com.datn.datnbe.auth.entity.UserProfile;
import com.datn.datnbe.auth.repository.UserProfileRepo;
import com.datn.datnbe.auth.service.KeycloakAuthorizationService;
import com.datn.datnbe.student.entity.ClassEnrollment;
import com.datn.datnbe.student.entity.Student;
import com.datn.datnbe.student.repository.ClassEnrollmentRepository;
import com.datn.datnbe.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClassGroupManagementService {

    private final KeycloakAuthorizationService keycloakAuthzService;
    private final ClassEnrollmentRepository classEnrollmentRepository;
    private final StudentRepository studentRepository;
    private final UserProfileRepo userProfileRepo;

    private static final String CLASS_GROUP_PREFIX = "class-";

    /**
     * Adds a student to the class's Keycloak group.
     * Called when a student enrolls in a class.
     *
     * @param classId the class ID
     * @param studentId the student ID (from Student entity)
     */
    @Transactional
    public void addStudentToClassGroup(String classId, String studentId) {
        log.info("Adding student {} to class group {}", studentId, classId);

        // Get or create the class group
        String groupName = CLASS_GROUP_PREFIX + classId;
        KeycloakGroupDto classGroup = keycloakAuthzService.createGroup(groupName);

        // Get the student's Keycloak user ID
        String keycloakUserId = getKeycloakUserIdForStudent(studentId);
        if (keycloakUserId == null) {
            log.warn("Could not find Keycloak user ID for student {}, skipping group assignment", studentId);
            return;
        }

        // Add user to group
        try {
            keycloakAuthzService.addUserToGroup(keycloakUserId, classGroup.getId());
            log.info("Successfully added student {} (Keycloak: {}) to class group {}",
                    studentId,
                    keycloakUserId,
                    groupName);
        } catch (Exception e) {
            log.error("Failed to add student {} to class group {}: {}", studentId, groupName, e.getMessage());
        }
    }

    /**
     * Removes a student from the class's Keycloak group.
     * Called when a student is removed from a class.
     *
     * @param classId the class ID
     * @param studentId the student ID (from Student entity)
     */
    @Transactional
    public void removeStudentFromClassGroup(String classId, String studentId) {
        log.info("Removing student {} from class group {}", studentId, classId);

        // Get the class group
        String groupName = CLASS_GROUP_PREFIX + classId;
        KeycloakGroupDto classGroup = keycloakAuthzService.createGroup(groupName);

        // Get the student's Keycloak user ID
        String keycloakUserId = getKeycloakUserIdForStudent(studentId);
        if (keycloakUserId == null) {
            log.warn("Could not find Keycloak user ID for student {}, skipping group removal", studentId);
            return;
        }

        // Remove user from group
        try {
            keycloakAuthzService.removeUserFromGroup(keycloakUserId, classGroup.getId());
            log.info("Successfully removed student {} (Keycloak: {}) from class group {}",
                    studentId,
                    keycloakUserId,
                    groupName);
        } catch (Exception e) {
            log.error("Failed to remove student {} from class group {}: {}", studentId, groupName, e.getMessage());
        }
    }

    /**
     * Synchronizes all enrolled students in a class to the class's Keycloak group.
     * Useful for initial setup or fixing inconsistencies.
     *
     * @param classId the class ID
     */
    @Transactional
    public void syncClassGroupMembers(String classId) {
        log.info("Syncing class group members for class {}", classId);

        // Get all enrollments for the class
        List<ClassEnrollment> enrollments = classEnrollmentRepository.findByClassId(classId);
        log.info("Found {} enrollments for class {}", enrollments.size(), classId);

        // Get or create the class group
        String groupName = CLASS_GROUP_PREFIX + classId;
        KeycloakGroupDto classGroup = keycloakAuthzService.createGroup(groupName);

        int successCount = 0;
        int failedCount = 0;

        for (ClassEnrollment enrollment : enrollments) {
            String keycloakUserId = getKeycloakUserIdForStudent(enrollment.getStudentId());
            if (keycloakUserId == null) {
                log.warn("Could not find Keycloak user ID for student {}, skipping", enrollment.getStudentId());
                failedCount++;
                continue;
            }

            try {
                keycloakAuthzService.addUserToGroup(keycloakUserId, classGroup.getId());
                successCount++;
            } catch (Exception e) {
                log.warn("Failed to add student {} to class group: {}", enrollment.getStudentId(), e.getMessage());
                failedCount++;
            }
        }

        log.info("Class group sync completed for class {}: {} succeeded, {} failed",
                classId,
                successCount,
                failedCount);
    }

    /**
     * Gets the Keycloak user ID for a student.
     */
    private String getKeycloakUserIdForStudent(String studentId) {
        // First, get the student to find their user ID
        Optional<Student> studentOpt = studentRepository.findById(studentId);
        if (studentOpt.isEmpty()) {
            log.warn("Student not found: {}", studentId);
            return null;
        }

        String userId = studentOpt.get().getUserId();
        if (userId == null) {
            log.warn("Student {} has no associated user ID", studentId);
            return null;
        }

        // Get the UserProfile to find the Keycloak user ID
        Optional<UserProfile> userProfileOpt = userProfileRepo.findById(userId);
        if (userProfileOpt.isEmpty()) {
            log.warn("UserProfile not found for user ID: {}", userId);
            return null;
        }

        return userProfileOpt.get().getKeycloakUserId();
    }
}
