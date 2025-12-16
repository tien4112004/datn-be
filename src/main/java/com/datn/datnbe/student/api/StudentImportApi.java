package com.datn.datnbe.student.api;

import com.datn.datnbe.student.dto.response.StudentImportResponseDto;
import org.springframework.web.multipart.MultipartFile;

/**
 * API interface for student import operations.
 */
public interface StudentImportApi {

    /**
     * Import students from a CSV file.
     * The CSV should contain columns: firstName, lastName, email, phoneNumber, avatarUrl, status
     * Required columns: firstName, lastName, email
     *
     * @param file the CSV file to import
     * @return import result with success status, count of created students, and any errors
     */
    StudentImportResponseDto importStudentsFromCsv(MultipartFile file);
}
