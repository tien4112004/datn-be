package com.datn.datnbe.student.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CSV Parser Service Tests")
class   CsvParserServiceTest {

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

    @Test
    @DisplayName("Parse CSV with escaped quotes within quoted fields")
    void parseStudentCsv_withEscapedQuotes_parsesCorrectly() {
        // CSV content with escaped quotes (double quotes within quoted fields)
        // Format: "field with ""quotes"" inside"
        String csvContent = "fullName,dateOfBirth,gender,address,parentName,parentPhone,parentContactEmail,classId,enrollmentDate,status\n"
                + "\"Nguyen Van A\",2008-01-15,male,\"123 Main St, \"\"Downtown\"\" District\",Nguyen Van X,+84987654321,parent1@example.com,cls_001,2024-01-15,active\n"
                + "\"Tran Thi B\",2008-05-20,female,\"456 Oak Ave, \"\"Central\"\" Zone\",Tran Van Y,+84912345678,parent2@example.com,cls_002,2024-01-20,active\n"
                + "\"Le Van C\",2008-03-10,male,\"789 Pine Rd - \"\"Old City\"\"\",Le Van Z,+84934567890,parent3@example.com,cls_001,2024-02-01,active";

        MockMultipartFile file = new MockMultipartFile("file", "students.csv", "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8));

        CsvParserService.ParseResult result = csvParserService.parseStudentCsv(file);

        // Verify no parsing errors occurred
        assertThat(result.hasErrors()).isFalse();
        assertThat(result.rows()).hasSize(3);

        // Verify first row with escaped quotes in address
        assertThat(result.rows().get(0).getFullName()).isEqualTo("Nguyen Van A");
        assertThat(result.rows().get(0).getAddress()).isEqualTo("123 Main St, \"Downtown\" District");
        assertThat(result.rows().get(0).getParentContactEmail()).isEqualTo("parent1@example.com");

        // Verify second row
        assertThat(result.rows().get(1).getFullName()).isEqualTo("Tran Thi B");
        assertThat(result.rows().get(1).getAddress()).isEqualTo("456 Oak Ave, \"Central\" Zone");

        // Verify third row with escaped quotes at the end
        assertThat(result.rows().get(2).getFullName()).isEqualTo("Le Van C");
        assertThat(result.rows().get(2).getAddress()).isEqualTo("789 Pine Rd - \"Old City\"");
    }
}
