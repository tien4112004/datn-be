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
                    firstName,lastName,email,phoneNumber,avatarUrl,status
                    Nguyen,Van A,student1@example.com,+84987654321,https://example.com/avatar1.jpg,ACTIVE
                    Tran,Thi B,student2@example.com,+84912345678,https://example.com/avatar2.jpg,ACTIVE
                    """;
            MockMultipartFile file = new MockMultipartFile("file", "students.csv", "text/csv",
                    csvContent.getBytes(StandardCharsets.UTF_8));

            // When
            CsvParserService.ParseResult result = csvParserService.parseStudentCsv(file);

            // Then
            assertThat(result.hasErrors()).isFalse();
            assertThat(result.rows()).hasSize(2);
            assertThat(result.rows().get(0).getFirstName()).isEqualTo("Nguyen");
            assertThat(result.rows().get(0).getLastName()).isEqualTo("Van A");
            assertThat(result.rows().get(0).getEmail()).isEqualTo("student1@example.com");
            assertThat(result.rows().get(1).getFirstName()).isEqualTo("Tran");
        }

        @Test
        void parseStudentCsv_withMinimalRequiredFields_returnsStudents() {
            // Given
            String csvContent = """
                    firstName,lastName,email
                    Nguyen,Van A,student@example.com
                    """;
            MockMultipartFile file = new MockMultipartFile("file", "students.csv", "text/csv",
                    csvContent.getBytes(StandardCharsets.UTF_8));

            // When
            CsvParserService.ParseResult result = csvParserService.parseStudentCsv(file);

            // Then
            assertThat(result.hasErrors()).isFalse();
            assertThat(result.rows()).hasSize(1);
            assertThat(result.rows().get(0).getPhoneNumber()).isNull();
            assertThat(result.rows().get(0).getAvatarUrl()).isNull();
        }

        @Test
        void parseStudentCsv_withQuotedValues_handlesCorrectly() {
            // Given
            String csvContent = """
                    firstName,lastName,email
                    Nguyen,"Van A","student@example.com"
                    """;
            MockMultipartFile file = new MockMultipartFile("file", "students.csv", "text/csv",
                    csvContent.getBytes(StandardCharsets.UTF_8));

            // When
            CsvParserService.ParseResult result = csvParserService.parseStudentCsv(file);

            // Then
            assertThat(result.hasErrors()).isFalse();
            assertThat(result.rows()).hasSize(1);
            assertThat(result.rows().get(0).getLastName()).isEqualTo("Van A");
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
                    phoneNumber,avatarUrl
                    +84987654321,https://example.com/avatar.jpg
                    """;
            MockMultipartFile file = new MockMultipartFile("file", "students.csv", "text/csv",
                    csvContent.getBytes(StandardCharsets.UTF_8));

            // When
            CsvParserService.ParseResult result = csvParserService.parseStudentCsv(file);

            // Then
            assertThat(result.hasErrors()).isTrue();
            assertThat(result.errors()).anyMatch(e -> e.contains("Missing required column"));
        }

        @Test
        void parseStudentCsv_withMissingRequiredValue_returnsError() {
            // Given
            String csvContent = """
                    firstName,lastName,email
                    ,Van A,student@example.com
                    """;
            MockMultipartFile file = new MockMultipartFile("file", "students.csv", "text/csv",
                    csvContent.getBytes(StandardCharsets.UTF_8));

            // When
            CsvParserService.ParseResult result = csvParserService.parseStudentCsv(file);

            // Then
            assertThat(result.hasErrors()).isTrue();
            assertThat(result.errors()).anyMatch(e -> e.contains("Row 2"));
        }

        @Test
        void parseStudentCsv_withBlankRows_skipsBlankRows() {
            // Given
            String csvContent = """
                    firstName,lastName,email
                    Nguyen,Van A,student1@example.com

                    Tran,Thi B,student2@example.com
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
