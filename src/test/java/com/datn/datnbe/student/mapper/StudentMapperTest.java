package com.datn.datnbe.student.mapper;

import com.datn.datnbe.student.dto.request.StudentCsvRow;
import com.datn.datnbe.student.entity.Student;
import com.datn.datnbe.student.enums.Role;
import com.datn.datnbe.student.enums.StudentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StudentMapperTest {

    private StudentMapper studentMapper;

    @BeforeEach
    void setUp() {
        studentMapper = new StudentMapper();
    }

    @Nested
    @DisplayName("Valid mapping tests")
    class ValidMappingTests {

        @Test
        void toEntity_withAllFields_mapsCorrectly() {
            // Given
            StudentCsvRow csvRow = StudentCsvRow.builder()
                    .firstName("Nguyen")
                    .lastName("Van A")
                    .email("student@example.com")
                    .phoneNumber("+84987654321")
                    .avatarUrl("https://example.com/avatar.jpg")
                    .status("ACTIVE")
                    .build();
            List<String> errors = new ArrayList<>();

            // When
            Student student = studentMapper.toEntity(csvRow, 2, errors);

            // Then
            assertThat(errors).isEmpty();
            assertThat(student).isNotNull();
            assertThat(student.getFirstName()).isEqualTo("Nguyen");
            assertThat(student.getLastName()).isEqualTo("Van A");
            assertThat(student.getEmail()).isEqualTo("student@example.com");
            assertThat(student.getPhoneNumber()).isEqualTo("+84987654321");
            assertThat(student.getAvatarUrl()).isEqualTo("https://example.com/avatar.jpg");
            assertThat(student.getStatus()).isEqualTo(StudentStatus.ACTIVE);
            assertThat(student.getRole()).isEqualTo(Role.STUDENT);
        }

        @Test
        void toEntity_withMinimalFields_usesDefaults() {
            // Given
            StudentCsvRow csvRow = StudentCsvRow.builder()
                    .firstName("Nguyen")
                    .lastName("Van A")
                    .email("student@example.com")
                    .build();
            List<String> errors = new ArrayList<>();

            // When
            Student student = studentMapper.toEntity(csvRow, 2, errors);

            // Then
            assertThat(errors).isEmpty();
            assertThat(student).isNotNull();
            assertThat(student.getStatus()).isEqualTo(StudentStatus.ACTIVE);
            assertThat(student.getRole()).isEqualTo(Role.STUDENT);
            assertThat(student.getPhoneNumber()).isNull();
            assertThat(student.getAvatarUrl()).isNull();
        }

        @Test
        void toEntity_withEmailValidation_mapsCorrectly() {
            // Given
            StudentCsvRow csvRow = StudentCsvRow.builder()
                    .firstName("Tran")
                    .lastName("Thi B")
                    .email("valid.email@example.com")
                    .build();
            List<String> errors = new ArrayList<>();

            // When
            Student student = studentMapper.toEntity(csvRow, 2, errors);

            // Then
            assertThat(errors).isEmpty();
            assertThat(student.getEmail()).isEqualTo("valid.email@example.com");
        }
    }

    @Nested
    @DisplayName("Invalid mapping tests")
    class InvalidMappingTests {

        @Test
        void toEntity_withMissingFirstName_returnsError() {
            // Given
            StudentCsvRow csvRow = StudentCsvRow.builder()
                    .firstName(null)
                    .lastName("Van A")
                    .email("student@example.com")
                    .build();
            List<String> errors = new ArrayList<>();

            // When
            Student student = studentMapper.toEntity(csvRow, 2, errors);

            // Then
            assertThat(errors).hasSize(1);
            assertThat(errors.get(0)).contains("Row 2");
            assertThat(errors.get(0)).contains("firstName");
            assertThat(student).isNull();
        }

        @Test
        void toEntity_withMissingLastName_returnsError() {
            // Given
            StudentCsvRow csvRow = StudentCsvRow.builder()
                    .firstName("Nguyen")
                    .lastName(null)
                    .email("student@example.com")
                    .build();
            List<String> errors = new ArrayList<>();

            // When
            Student student = studentMapper.toEntity(csvRow, 2, errors);

            // Then
            assertThat(errors).hasSize(1);
            assertThat(errors.get(0)).contains("Row 2");
            assertThat(errors.get(0)).contains("lastName");
            assertThat(student).isNull();
        }

        @Test
        void toEntity_withMissingEmail_returnsError() {
            // Given
            StudentCsvRow csvRow = StudentCsvRow.builder().firstName("Nguyen").lastName("Van A").email(null).build();
            List<String> errors = new ArrayList<>();

            // When
            Student student = studentMapper.toEntity(csvRow, 2, errors);

            // Then
            assertThat(errors).hasSize(1);
            assertThat(errors.get(0)).contains("Row 2");
            assertThat(errors.get(0)).contains("email");
            assertThat(student).isNull();
        }

        @Test
        void toEntity_withInvalidEmailFormat_returnsError() {
            // Given
            StudentCsvRow csvRow = StudentCsvRow.builder()
                    .firstName("Nguyen")
                    .lastName("Van A")
                    .email("invalid-email-format")
                    .build();
            List<String> errors = new ArrayList<>();

            // When
            Student student = studentMapper.toEntity(csvRow, 2, errors);

            // Then
            assertThat(errors).hasSize(1);
            assertThat(errors.get(0)).contains("Row 2");
            assertThat(errors.get(0)).contains("email");
            assertThat(student).isNull();
        }

        @Test
        void toEntity_withInvalidStatus_returnsError() {
            // Given
            StudentCsvRow csvRow = StudentCsvRow.builder()
                    .firstName("Nguyen")
                    .lastName("Van A")
                    .email("student@example.com")
                    .status("INVALID_STATUS")
                    .build();
            List<String> errors = new ArrayList<>();

            // When
            Student student = studentMapper.toEntity(csvRow, 2, errors);

            // Then
            assertThat(errors).hasSize(1);
            assertThat(errors.get(0)).contains("Row 2");
            assertThat(errors.get(0)).contains("status");
            assertThat(student).isNull();
        }

        @Test
        void toEntity_withInvalidPhoneFormat_returnsError() {
            // Given
            StudentCsvRow csvRow = StudentCsvRow.builder()
                    .firstName("Nguyen")
                    .lastName("Van A")
                    .email("student@example.com")
                    .phoneNumber("invalid-phone!")
                    .build();
            List<String> errors = new ArrayList<>();

            // When
            Student student = studentMapper.toEntity(csvRow, 2, errors);

            // Then
            assertThat(errors).hasSize(1);
            assertThat(errors.get(0)).contains("Row 2");
            assertThat(errors.get(0)).contains("phoneNumber");
            assertThat(student).isNull();
        }

        @Test
        void toEntity_withMultipleErrors_reportsAll() {
            // Given
            StudentCsvRow csvRow = StudentCsvRow.builder()
                    .firstName(null)
                    .lastName(null)
                    .email("invalid-email")
                    .status("INVALID")
                    .phoneNumber("invalid!")
                    .build();
            List<String> errors = new ArrayList<>();

            // When
            Student student = studentMapper.toEntity(csvRow, 2, errors);

            // Then
            assertThat(errors).hasSizeGreaterThanOrEqualTo(3);
            assertThat(student).isNull();
        }
    }
}
