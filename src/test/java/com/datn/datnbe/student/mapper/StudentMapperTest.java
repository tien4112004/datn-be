package com.datn.datnbe.student.mapper;

import com.datn.datnbe.student.dto.request.StudentCsvRow;
import com.datn.datnbe.student.entity.Student;
import com.datn.datnbe.student.enums.Gender;
import com.datn.datnbe.student.enums.StudentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StudentMapperTest {

    private StudentMapper studentMapper;

    @BeforeEach
    void setUp() {
        studentMapper = new StudentMapper();
    }

    @Test
    @DisplayName("Valid mapping with minimal fields")
    void toEntity_withMinimalFields_usesDefaults() {
        // Given
        StudentCsvRow csvRow = StudentCsvRow.builder().userId("user_002").build();
        List<String> errors = new ArrayList<>();

        @Test
        void toEntity_withAllFields_mapsCorrectly() {
            // Given
            StudentCsvRow csvRow = StudentCsvRow.builder()
                    .id("std_001")
                    .fullName("Nguyen Van A")
                    .dateOfBirth("2008-01-15")
                    .gender("male")
                    .address("123 Main St")
                    .parentName("Nguyen Van X")
                    .parentPhone("+84987654321")
                    .classId("cls_001")
                    .enrollmentDate("2024-01-15")
                    .status("active")
                    .build();
            List<String> errors = new ArrayList<>();

            // When
            Student student = studentMapper.toEntity(csvRow, 2, errors);

            // Then
            assertThat(errors).isEmpty();
            assertThat(student).isNotNull();
            assertThat(student.getId()).isEqualTo("std_001");
            assertThat(student.getFullName()).isEqualTo("Nguyen Van A");
            assertThat(student.getDateOfBirth()).isEqualTo(LocalDate.of(2008, 1, 15));
            assertThat(student.getGender()).isEqualTo(Gender.MALE);
            assertThat(student.getAddress()).isEqualTo("123 Main St");
            assertThat(student.getParentName()).isEqualTo("Nguyen Van X");
            assertThat(student.getParentPhone()).isEqualTo("+84987654321");
            assertThat(student.getClassId()).isEqualTo("cls_001");
            assertThat(student.getEnrollmentDate()).isEqualTo(LocalDate.of(2024, 1, 15));
            assertThat(student.getStatus()).isEqualTo(StudentStatus.ACTIVE);
        }

        @Test
        void toEntity_withMinimalFields_usesDefaults() {
            // Given
            StudentCsvRow csvRow = StudentCsvRow.builder().id("std_001").fullName("Nguyen Van A").build();
            List<String> errors = new ArrayList<>();

            // When
            Student student = studentMapper.toEntity(csvRow, 2, errors);

            // Then
            assertThat(errors).isEmpty();
            assertThat(student).isNotNull();
            assertThat(student.getStatus()).isEqualTo(StudentStatus.ACTIVE);
            assertThat(student.getGender()).isNull();
            assertThat(student.getDateOfBirth()).isNull();
        }

        @Test
        void toEntity_withFemaleGender_mapsCorrectly() {
            // Given
            StudentCsvRow csvRow = StudentCsvRow.builder()
                    .id("std_001")
                    .fullName("Tran Thi B")
                    .gender("female")
                    .build();
            List<String> errors = new ArrayList<>();

            // When
            Student student = studentMapper.toEntity(csvRow, 2, errors);

            // Then
            assertThat(errors).isEmpty();
            assertThat(student.getGender()).isEqualTo(Gender.FEMALE);
        }
    }

    @Nested
    @DisplayName("Invalid mapping tests")
    class InvalidMappingTests {

        @Test
        void toEntity_withInvalidDateFormat_returnsError() {
            // Given
            StudentCsvRow csvRow = StudentCsvRow.builder()
                    .id("std_001")
                    .fullName("Nguyen Van A")
                    .dateOfBirth("15-01-2008") // Wrong format
                    .build();
            List<String> errors = new ArrayList<>();

            // When
            Student student = studentMapper.toEntity(csvRow, 2, errors);

            // Then
            assertThat(errors).hasSize(1);
            assertThat(errors.get(0)).contains("Row 2: Invalid dateOfBirth format");
            assertThat(student).isNull();
        }

        @Test
        void toEntity_withInvalidGender_returnsError() {
            // Given
            StudentCsvRow csvRow = StudentCsvRow.builder()
                    .id("std_001")
                    .fullName("Nguyen Van A")
                    .gender("unknown")
                    .build();
            List<String> errors = new ArrayList<>();

            // When
            Student student = studentMapper.toEntity(csvRow, 2, errors);

            // Then
            assertThat(errors).hasSize(1);
            assertThat(errors.get(0)).contains("Row 2: Invalid gender value");
            assertThat(student).isNull();
        }

        @Test
        void toEntity_withInvalidStatus_returnsError() {
            // Given
            StudentCsvRow csvRow = StudentCsvRow.builder()
                    .id("std_001")
                    .fullName("Nguyen Van A")
                    .status("invalid_status")
                    .build();
            List<String> errors = new ArrayList<>();

            // When
            Student student = studentMapper.toEntity(csvRow, 2, errors);

            // Then
            assertThat(errors).hasSize(1);
            assertThat(errors.get(0)).contains("Row 2: Invalid status value");
            assertThat(student).isNull();
        }

        @Test
        void toEntity_withInvalidPhoneFormat_returnsError() {
            // Given
            StudentCsvRow csvRow = StudentCsvRow.builder()
                    .id("std_001")
                    .fullName("Nguyen Van A")
                    .parentPhone("invalid-phone!")
                    .build();
            List<String> errors = new ArrayList<>();

            // When
            Student student = studentMapper.toEntity(csvRow, 2, errors);

            // Then
            assertThat(errors).hasSize(1);
            assertThat(errors.get(0)).contains("Row 2: Invalid parentPhone format");
            assertThat(student).isNull();
        }

        @Test
        void toEntity_withMultipleErrors_reportsAll() {
            // Given
            StudentCsvRow csvRow = StudentCsvRow.builder()
                    .id("std_001")
                    .fullName("Nguyen Van A")
                    .dateOfBirth("invalid")
                    .gender("invalid")
                    .status("invalid")
                    .build();
            List<String> errors = new ArrayList<>();

            // When
            Student student = studentMapper.toEntity(csvRow, 2, errors);

            // Then
            assertThat(errors).hasSize(3);
            assertThat(student).isNull();
        }
    }
}
