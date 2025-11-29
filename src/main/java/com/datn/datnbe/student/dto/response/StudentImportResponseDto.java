package com.datn.datnbe.student.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO for student CSV import operation.
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
    List<String> errors = new ArrayList<>();

    public static StudentImportResponseDto success(int studentsCreated) {
        return StudentImportResponseDto.builder()
                .success(true)
                .studentsCreated(studentsCreated)
                .message("Students imported successfully")
                .errors(new ArrayList<>())
                .build();
    }

    public static StudentImportResponseDto failure(List<String> errors) {
        return StudentImportResponseDto.builder()
                .success(false)
                .studentsCreated(0)
                .message("Student import failed with errors")
                .errors(errors)
                .build();
    }
}
