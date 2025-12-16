package com.datn.datnbe.student.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

    @Test
    void parseStudentCsv_withValidCsv_returnsStudents() {
        String csvContent = "userId,enrollmentDate,address,parentContactEmail,status\nuser_001,2024-01-15,123 Main St,parent1@example.com,ACTIVE\nuser_002,2024-01-20,456 Oak Ave,parent2@example.com,ACTIVE";
        MockMultipartFile file = new MockMultipartFile("file", "students.csv", "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8));

        CsvParserService.ParseResult result = csvParserService.parseStudentCsv(file);

        assertThat(result.hasErrors()).isFalse();
        assertThat(result.rows()).hasSize(2);
        assertThat(result.rows().get(0).getUserId()).isEqualTo("user_001");
        assertThat(result.rows().get(0).getAddress()).isEqualTo("123 Main St");
        assertThat(result.rows().get(1).getUserId()).isEqualTo("user_002");
    }

    @Test
    void parseStudentCsv_withMinimalRequiredFields_returnsStudents() {
        String csvContent = "userId,enrollmentDate,address,parentContactEmail\nuser_001,2024-01-15,123 Main St,parent@example.com";
        MockMultipartFile file = new MockMultipartFile("file", "students.csv", "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8));

        CsvParserService.ParseResult result = csvParserService.parseStudentCsv(file);

        assertThat(result.hasErrors()).isFalse();
        assertThat(result.rows()).hasSize(1);
        assertThat(result.rows().get(0).getUserId()).isEqualTo("user_001");
    }

    @Test
    void parseStudentCsv_withEmptyFile_returnsError() {
        MockMultipartFile file = new MockMultipartFile("file", "students.csv", "text/csv", new byte[0]);

        CsvParserService.ParseResult result = csvParserService.parseStudentCsv(file);

        assertThat(result.hasErrors()).isTrue();
    }
}
