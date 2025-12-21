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
        String csvContent = "fullName,dateOfBirth,gender,address,parentName,parentPhone,parentContactEmail,classId,enrollmentDate,status\nNguyen Van A,2008-01-15,male,123 Main St,Nguyen Van X,+84987654321,parent1@example.com,cls_001,2024-01-15,active\nTran Thi B,2008-05-20,female,456 Oak Ave,Tran Van Y,+84912345678,parent2@example.com,cls_001,2024-01-20,active";
        MockMultipartFile file = new MockMultipartFile("file", "students.csv", "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8));

        CsvParserService.ParseResult result = csvParserService.parseStudentCsv(file);

        assertThat(result.hasErrors()).isFalse();
        assertThat(result.rows()).hasSize(2);
        assertThat(result.rows().get(0).getFullName()).isEqualTo("Nguyen Van A");
        assertThat(result.rows().get(0).getAddress()).isEqualTo("123 Main St");
        assertThat(result.rows().get(0).getParentContactEmail()).isEqualTo("parent1@example.com");
        assertThat(result.rows().get(1).getFullName()).isEqualTo("Tran Thi B");
    }

    @Test
    void parseStudentCsv_withMinimalRequiredFields_returnsStudents() {
        String csvContent = "fullName,dateOfBirth,gender,address,parentName,parentPhone,parentContactEmail,classId,enrollmentDate,status\nNguyen Van A,2008-01-15,male,123 Main St,Nguyen Van X,+84987654321,parent1@example.com,cls_001,2024-01-15,active";
        MockMultipartFile file = new MockMultipartFile("file", "students.csv", "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8));

        CsvParserService.ParseResult result = csvParserService.parseStudentCsv(file);

        assertThat(result.hasErrors()).isFalse();
        assertThat(result.rows()).hasSize(1);
        assertThat(result.rows().get(0).getFullName()).isEqualTo("Nguyen Van A");
    }

    @Test
    void parseStudentCsv_withEmptyFile_returnsError() {
        MockMultipartFile file = new MockMultipartFile("file", "students.csv", "text/csv", new byte[0]);

        CsvParserService.ParseResult result = csvParserService.parseStudentCsv(file);

        assertThat(result.hasErrors()).isTrue();
    }
}
