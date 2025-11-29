package com.datn.datnbe.student.api;

import com.datn.datnbe.student.dto.request.StudentUpdateRequest;
import com.datn.datnbe.student.dto.response.StudentResponseDto;

/**
 * API interface for student CRUD operations.
 */
public interface StudentApi {

    /**
     * Get a student by ID.
     *
     * @param id the student ID
     * @return the student data
     */
    StudentResponseDto getStudentById(String id);

    /**
     * Update a student by ID.
     *
     * @param id      the student ID
     * @param request the update request
     * @return the updated student data
     */
    StudentResponseDto updateStudent(String id, StudentUpdateRequest request);

    /**
     * Delete a student by ID.
     *
     * @param id the student ID
     */
    void deleteStudent(String id);
}
