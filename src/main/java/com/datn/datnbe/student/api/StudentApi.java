package com.datn.datnbe.student.api;

import com.datn.datnbe.student.dto.request.StudentCreateRequest;
import com.datn.datnbe.student.dto.request.StudentUpdateRequest;
import com.datn.datnbe.sharedkernel.dto.students.ClassEnrollmentDto;
import com.datn.datnbe.student.dto.response.StudentResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * API interface for student CRUD operations.
 */
public interface StudentApi {

    /**
     * Get all students in a class.
     *
     * @param classId  the class ID
     * @param pageable pagination parameters
     * @return paginated list of students
     */
    PaginatedResponseDto<StudentResponseDto> getStudentsByClass(String classId, Pageable pageable);

    /**
     * Enroll an existing student in a class.
     *
     * @param classId the class ID
     * @param studentId the student ID to enroll
     * @return the enrolled student data
     */
    StudentResponseDto enrollStudent(String classId, String studentId);

    /**
     * Remove a student from a class.
     *
     * @param classId the class ID
     * @param studentId the student ID
     */
    void removeStudentFromClass(String classId, String studentId);

    /**
     * Update a student by ID.
     *
     * @param id the student ID
     * @param request the update request
     * @return the updated student data
     */
    StudentResponseDto updateStudent(String id, StudentUpdateRequest request);

    StudentResponseDto createStudent(StudentCreateRequest request);

    StudentResponseDto getStudentById(String id);

    void deleteStudent(String id);

    /**
     * Regenerate passwords for multiple students
     * @param studentIds list of student IDs
     * @return list of updated student responses with new passwords
     */
    List<StudentResponseDto> regeneratePasswords(List<String> studentIds);

    /**
     * Gets the Keycloak user ID for a student.
     *
     * @param studentId the student ID
     * @return Optional containing the Keycloak user ID, or empty if not found
     */
    Optional<String> getKeycloakUserIdForStudent(String studentId);

    /**
     * Gets all Keycloak user IDs for students enrolled in a class.
     *
     * @param classId the class ID
     * @return list of Keycloak user IDs for enrolled students
     */
    List<String> getEnrolledStudentKeycloakUserIds(String classId);

    List<StudentResponseDto> getStudentsByClassId(String classId);

    /**
     * Gets all enrollments for a specific class.
     *
     * @param classId the class ID
     * @return list of enrollment DTOs
     */
    List<ClassEnrollmentDto> getEnrollmentsByClassId(String classId);

    /**
     * Gets all enrollments for a specific user (by their Keycloak user ID).
     *
     * @param userId the Keycloak user ID
     * @return list of enrollment DTOs
     */
    List<ClassEnrollmentDto> getEnrollmentsByUserId(String userId);

    /**
     * Gets the count of active enrollments in a class.
     *
     * @param classId the class ID
     * @return count of active enrollments
     */
    long getActiveEnrollmentCount(String classId);

    /**
     * Gets the total count of enrollments in a class (all statuses).
     *
     * @param classId the class ID
     * @return total count of enrollments
     */
    long getTotalEnrollmentCount(String classId);

    /**
     * Checks if a user is enrolled in a class.
     *
     * @param classId the class ID
     * @param userId the Keycloak user ID
     * @return true if the user is enrolled, false otherwise
     */
    boolean isUserEnrolledInClass(String classId, String userId);
}
