package com.datn.datnbe.student.mapper;

import com.datn.datnbe.student.dto.request.StudentCsvRow;
import com.datn.datnbe.student.entity.Student;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StudentMapperTest {

    private final StudentMapper studentMapper = new StudentMapper();

    @Test
    @DisplayName("Valid mapping with all fields")
    void toEntity_withAllFields_mapsCorrectly() {
        // Given
        StudentCsvRow csvRow = StudentCsvRow.builder()
                .userId("user_001")
                .enrollmentDate(LocalDate.of(2024, 1, 15))
                .address("123 Main St")
                .parentContactEmail("parent@example.com")
                .status("ACTIVE")
                .build();
        List<String> errors = new ArrayList<>();

        // When
        Student student = studentMapper.toEntity(csvRow, 1, errors);

        // Then
        assertThat(errors).isEmpty();
        assertThat(student).isNotNull();
        assertThat(student.getUserId()).isEqualTo("user_001");
        assertThat(student.getEnrollmentDate()).isEqualTo(LocalDate.of(2024, 1, 15));
        assertThat(student.getAddress()).isEqualTo("123 Main St");
        assertThat(student.getParentContactEmail()).isEqualTo("parent@example.com");
    }

    @Test
    @DisplayName("Valid mapping with minimal fields")
    void toEntity_withMinimalFields_usesDefaults() {
        // Given
        StudentCsvRow csvRow = StudentCsvRow.builder().userId("user_002").build();
        List<String> errors = new ArrayList<>();

        // When
        Student student = studentMapper.toEntity(csvRow, 2, errors);

        // Then
        assertThat(errors).isEmpty();
        assertThat(student).isNotNull();
        assertThat(student.getUserId()).isEqualTo("user_002");
    }

    @Test
    @DisplayName("Invalid mapping - missing userId")
    void toEntity_withMissingUserId_returnsError() {
        // Given
        StudentCsvRow csvRow = StudentCsvRow.builder().enrollmentDate(LocalDate.of(2024, 1, 15)).build();
        List<String> errors = new ArrayList<>();

        // When
        Student student = studentMapper.toEntity(csvRow, 2, errors);

        // Then
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).contains("Row 2");
        assertThat(errors.get(0)).contains("userId");
        assertThat(student).isNull();
    }

    @Test
    @DisplayName("Invalid mapping - invalid email format")
    void toEntity_withInvalidEmailFormat_returnsError() {
        // Given
        StudentCsvRow csvRow = StudentCsvRow.builder()
                .userId("user_002")
                .parentContactEmail("invalid-email-format")
                .build();
        List<String> errors = new ArrayList<>();

        // When
        Student student = studentMapper.toEntity(csvRow, 2, errors);

        // Then
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).contains("Row 2");
        assertThat(errors.get(0)).contains("parentContactEmail");
        assertThat(student).isNull();
    }
}
