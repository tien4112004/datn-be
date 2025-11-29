package com.datn.datnbe.student.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CSV Parser Service Tests")
class CsvParserServiceTest {

    private CsvParserService csvParserService;

    @BeforeEach
    void setUp() {
        csvParserService = new CsvParserService();
    }

    @Nested
    @DisplayName("Valid CSV parsing tests")
    class ValidCsvTests {

        @Test
        void parseStudentCsv_withValidCsv_returnsStudents() {
            // Given
            String csvContent = """
                    id,fullName,dateOfBirth,gender,address,parentName,parentPhone,classId,enrollmentDate,status
                    std_001,Nguyen Van A,2008-01-15,male,123 Main St,Nguyen Van X,+84987654321,cls_001,2024-01-15,active
                    std_002,Tran Thi B,2008-05-20,female,456 Oak Ave,Tran Van Y,+84912345678,cls_001,2024-01-15,active
                    """;
            MockMultipartFile file = new MockMultipartFile("file", "students.csv", "text/csv",
                    csvContent.getBytes(StandardCharsets.UTF_8));

            // When
            CsvParserService.ParseResult result = csvParserService.parseStudentCsv(file);

            // Then
            assertThat(result.hasErrors()).isFalse();
            assertThat(result.rows()).hasSize(2);
            assertThat(result.rows().get(0).getId()).isEqualTo("std_001");
            assertThat(result.rows().get(0).getFullName()).isEqualTo("Nguyen Van A");
            assertThat(result.rows().get(1).getId()).isEqualTo("std_002");
        }

        @Test
        void parseStudentCsv_withMinimalRequiredFields_returnsStudents() {
            // Given
            String csvContent = """
                    id,fullName
                    std_001,Nguyen Van A
                    """;
            MockMultipartFile file = new MockMultipartFile("file", "students.csv", "text/csv",
                    csvContent.getBytes(StandardCharsets.UTF_8));

            // When
            CsvParserService.ParseResult result = csvParserService.parseStudentCsv(file);

            // Then
            assertThat(result.hasErrors()).isFalse();
            assertThat(result.rows()).hasSize(1);
            assertThat(result.rows().get(0).getDateOfBirth()).isNull();
        }

        @Test
        void parseStudentCsv_withQuotedValues_handlesCorrectly() {
            // Given
            String csvContent = """
                    id,fullName,address
                    std_001,"Nguyen Van A","123 Main St, City, Country"
                    """;
            MockMultipartFile file = new MockMultipartFile("file", "students.csv", "text/csv",
                    csvContent.getBytes(StandardCharsets.UTF_8));

            // When
            CsvParserService.ParseResult result = csvParserService.parseStudentCsv(file);

            // Then
            assertThat(result.hasErrors()).isFalse();
            assertThat(result.rows()).hasSize(1);
            assertThat(result.rows().get(0).getAddress()).isEqualTo("123 Main St, City, Country");
        }
    }

    @Nested
    @DisplayName("Invalid CSV parsing tests")
    class InvalidCsvTests {

        @Test
        void parseStudentCsv_withEmptyFile_returnsError() {
            // Given
            MockMultipartFile file = new MockMultipartFile("file", "students.csv", "text/csv", new byte[0]);

            // When
            CsvParserService.ParseResult result = csvParserService.parseStudentCsv(file);

            // Then
            assertThat(result.hasErrors()).isTrue();
            assertThat(result.errors()).contains("CSV file is empty or not provided");
        }

        @Test
        void parseStudentCsv_withMissingRequiredHeader_returnsError() {
            // Given
            String csvContent = """
                    id,dateOfBirth
                    std_001,2008-01-15
                    """;
            MockMultipartFile file = new MockMultipartFile("file", "students.csv", "text/csv",
                    csvContent.getBytes(StandardCharsets.UTF_8));

            // When
            CsvParserService.ParseResult result = csvParserService.parseStudentCsv(file);

            // Then
            assertThat(result.hasErrors()).isTrue();
            assertThat(result.errors()).anyMatch(e -> e.contains("Missing required column: fullName"));
        }

        @Test
        void parseStudentCsv_withMissingRequiredValue_returnsError() {
            // Given
            String csvContent = """
                    id,fullName
                    ,Nguyen Van A
                    """;
            MockMultipartFile file = new MockMultipartFile("file", "students.csv", "text/csv",
                    csvContent.getBytes(StandardCharsets.UTF_8));

            // When
            CsvParserService.ParseResult result = csvParserService.parseStudentCsv(file);

            // Then
            assertThat(result.hasErrors()).isTrue();
            assertThat(result.errors()).anyMatch(e -> e.contains("Row 2: id is required"));
        }

        @Test
        void parseStudentCsv_withBlankRows_skipsBlankRows() {
            // Given
            String csvContent = """
                    id,fullName
                    std_001,Nguyen Van A

                    std_002,Tran Thi B
                    """;
            MockMultipartFile file = new MockMultipartFile("file", "students.csv", "text/csv",
                    csvContent.getBytes(StandardCharsets.UTF_8));

            // When
            CsvParserService.ParseResult result = csvParserService.parseStudentCsv(file);

            // Then
            assertThat(result.hasErrors()).isFalse();
            assertThat(result.rows()).hasSize(2);
        }
    }
}
