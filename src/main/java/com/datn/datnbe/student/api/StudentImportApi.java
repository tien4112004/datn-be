package com.datn.datnbe.student.api;

import com.datn.datnbe.student.dto.response.StudentImportResponseDto;
import org.springframework.web.multipart.MultipartFile;

/**
 * API interface for student import operations.
 */
public interface StudentImportApi {

    /**
     * Import students from a CSV file and enroll them in the specified class.
     * The CSV should contain columns: fullName, dateOfBirth, gender, address,
     * parentName, parentPhone, parentContactEmail
     * Required columns: fullName
     *
     * @param classId the class ID to enroll students in
     * @param file    the CSV file to import
     * @return import result with success status, count of created students, and any
     *         errors
     */
    StudentImportResponseDto importStudentsFromCsv(String classId, MultipartFile file);
}
