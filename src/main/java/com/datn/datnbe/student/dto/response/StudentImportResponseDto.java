package com.datn.datnbe.student.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO for student CSV import operation.
 * Includes credentials (username/password) for newly created students.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StudentImportResponseDto {

    boolean success;

    int studentsCreated;

    String message;

    @Builder.Default
    List<StudentCredentialDto> credentials = new ArrayList<>();

    @Builder.Default
    List<String> errors = new ArrayList<>();

    public static StudentImportResponseDto success(int studentsCreated, List<StudentCredentialDto> credentials) {
        return StudentImportResponseDto.builder()
                .success(true)
                .studentsCreated(studentsCreated)
                .message("Students imported successfully")
                .credentials(credentials != null ? credentials : new ArrayList<>())
                .errors(new ArrayList<>())
                .build();
    }

    public static StudentImportResponseDto failure(List<String> errors) {
        return StudentImportResponseDto.builder()
                .success(false)
                .studentsCreated(0)
                .message("Student import failed with errors")
                .credentials(new ArrayList<>())
                .errors(errors)
                .build();
    }
}
