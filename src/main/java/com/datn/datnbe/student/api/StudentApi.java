package com.datn.datnbe.student.api;

import com.datn.datnbe.student.dto.request.StudentCreateRequest;
import com.datn.datnbe.student.dto.request.StudentUpdateRequest;
import com.datn.datnbe.student.dto.response.StudentResponseDto;

import java.util.List;

/**
 * API interface for student CRUD operations.
 */
public interface StudentApi {

    /**
     * Get all students in a class.
     *
     * @param classId the class ID
     * @return list of students
     */
    List<StudentResponseDto> getStudentsByClass(String classId);

    /**
     * Enroll an existing student in a class.
     *
     * @param classId the class ID
     * @param studentId the student ID to enroll
     * @return the enrolled student data
     */
    StudentResponseDto enrollStudent(String classId, String studentId);

    /**
     * Create a new student and enroll in a class.
     *
     * @param classId the class ID
     * @param request the student create request
     * @return the created and enrolled student data
     */
    StudentResponseDto createAndEnrollStudent(String classId, StudentCreateRequest request);

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

    // ============================================================
    // DEPRECATED - Not in OpenAPI documentation
    // ============================================================

    /**
     * @deprecated Not in OpenAPI documentation. Use
     * {@link #createAndEnrollStudent(String, StudentCreateRequest)} instead to create
     * students within a class context.
     * @param request the create request
     * @return the created student data
     */
    @Deprecated(since = "1.0.0", forRemoval = true)
    StudentResponseDto createStudent(StudentCreateRequest request);

    /**
     * @deprecated Not in OpenAPI documentation. Students should be managed through class
     * enrollment operations only.
     * @param id the student ID
     * @return the student data
     */
    @Deprecated(since = "1.0.0", forRemoval = true)
    StudentResponseDto getStudentById(String id);

    /**
     * @deprecated Not in OpenAPI documentation. Students should be managed through class
     * enrollment operations only.
     * @param id the student ID
     */
    @Deprecated(since = "1.0.0", forRemoval = true)
    void deleteStudent(String id);
}
