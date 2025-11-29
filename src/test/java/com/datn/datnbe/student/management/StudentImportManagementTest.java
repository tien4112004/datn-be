package com.datn.datnbe.student.management;

import com.datn.datnbe.auth.api.UserProfileApi;
import com.datn.datnbe.auth.dto.response.UserProfileResponse;
import com.datn.datnbe.student.dto.request.StudentCsvRow;
import com.datn.datnbe.student.dto.response.StudentImportResponseDto;
import com.datn.datnbe.student.entity.Student;
import com.datn.datnbe.student.mapper.StudentMapper;
import com.datn.datnbe.student.repository.StudentRepository;
import com.datn.datnbe.student.service.CsvParserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentImportManagementTest {

    @Mock
    private CsvParserService csvParserService;

    @Mock
    private StudentMapper studentMapper;

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentImportManagement studentImportManagement;

    private MockMultipartFile validFile;

    @BeforeEach
    void setUp() {
        String csvContent = "id,fullName\nstd_001,Nguyen Van A";
        validFile = new MockMultipartFile("file", "students.csv", "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8));
    }

    @Nested
    @DisplayName("Successful import tests")
    class SuccessfulImportTests {

        @Test
        void importStudentsFromCsv_withValidData_returnsSuccess() {
            // Given
            StudentCsvRow csvRow = StudentCsvRow.builder().id("std_001").fullName("Nguyen Van A").build();
            Student student = Student.builder().id("std_001").fullName("Nguyen Van A").build();

            when(csvParserService.parseStudentCsv(any()))
                    .thenReturn(new CsvParserService.ParseResult(List.of(csvRow), new ArrayList<>()));
            when(studentMapper.toEntity(eq(csvRow), anyInt(), anyList())).thenReturn(student);
            when(studentRepository.findByIdIn(anySet())).thenReturn(Collections.emptyList());
            when(studentRepository.saveAll(anyList())).thenReturn(List.of(student));

            // When
            StudentImportResponseDto result = studentImportManagement.importStudentsFromCsv(validFile);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getStudentsCreated()).isEqualTo(1);
            assertThat(result.getErrors()).isEmpty();
            verify(studentRepository).saveAll(anyList());
        }

        @Test
        void importStudentsFromCsv_withMultipleStudents_savesAll() {
            // Given
            StudentCsvRow csvRow1 = StudentCsvRow.builder().id("std_001").fullName("A").build();
            StudentCsvRow csvRow2 = StudentCsvRow.builder().id("std_002").fullName("B").build();
            Student student1 = Student.builder().id("std_001").fullName("A").build();
            Student student2 = Student.builder().id("std_002").fullName("B").build();

            when(csvParserService.parseStudentCsv(any()))
                    .thenReturn(new CsvParserService.ParseResult(List.of(csvRow1, csvRow2), new ArrayList<>()));
            when(userProfileApi.createUserProfile(any())).thenReturn(createdUser1).thenReturn(createdUser2);
            when(studentMapper.toEntity(eq(csvRow1), anyInt(), anyList())).thenReturn(student1);
            when(studentMapper.toEntity(eq(csvRow2), anyInt(), anyList())).thenReturn(student2);
            when(studentRepository.findByIdIn(anySet())).thenReturn(Collections.emptyList());
            when(studentRepository.saveAll(anyList())).thenReturn(List.of(student1, student2));

            // When
            StudentImportResponseDto result = studentImportManagement.importStudentsFromCsv(validFile);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getStudentsCreated()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Failed import tests")
    class FailedImportTests {

        @Test
        void importStudentsFromCsv_withParseErrors_returnsFailure() {
            // Given
            List<String> parseErrors = List.of("Row 2: id is required");
            when(csvParserService.parseStudentCsv(any()))
                    .thenReturn(new CsvParserService.ParseResult(new ArrayList<>(), new ArrayList<>(parseErrors)));

            // When
            StudentImportResponseDto result = studentImportManagement.importStudentsFromCsv(validFile);

            // Then
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getStudentsCreated()).isEqualTo(0);
            assertThat(result.getErrors()).contains("Row 2: id is required");
            verify(studentRepository, never()).saveAll(anyList());
        }

        @Test
        void importStudentsFromCsv_withDuplicateIdsInCsv_returnsFailure() {
            // Given
            StudentCsvRow csvRow1 = StudentCsvRow.builder().id("std_001").fullName("A").build();
            StudentCsvRow csvRow2 = StudentCsvRow.builder().id("std_001").fullName("B").build();
            Student student1 = Student.builder().id("std_001").fullName("A").build();
            Student student2 = Student.builder().id("std_001").fullName("B").build();

            when(csvParserService.parseStudentCsv(any()))
                    .thenReturn(new CsvParserService.ParseResult(List.of(csvRow1, csvRow2), new ArrayList<>()));
            when(studentMapper.toEntity(eq(csvRow1), anyInt(), anyList())).thenReturn(student1);
            when(studentMapper.toEntity(eq(csvRow2), anyInt(), anyList())).thenReturn(student2);

            // When
            StudentImportResponseDto result = studentImportManagement.importStudentsFromCsv(validFile);

            // Then
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getErrors()).anyMatch(e -> e.contains("Duplicate student IDs"));
            verify(studentRepository, never()).saveAll(anyList());
        }

        @Test
        void importStudentsFromCsv_withExistingIdsInDb_returnsFailure() {
            // Given
            StudentCsvRow csvRow = StudentCsvRow.builder().id("std_001").fullName("A").build();
            Student student = Student.builder().id("std_001").fullName("A").build();
            Student existingStudent = Student.builder().id("std_001").fullName("Existing").build();

            when(csvParserService.parseStudentCsv(any()))
                    .thenReturn(new CsvParserService.ParseResult(List.of(csvRow), new ArrayList<>()));
            when(studentMapper.toEntity(eq(csvRow), anyInt(), anyList())).thenReturn(student);
            when(studentRepository.findByIdIn(anySet())).thenReturn(List.of(existingStudent));

            // When
            StudentImportResponseDto result = studentImportManagement.importStudentsFromCsv(validFile);

            // Then
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getErrors()).anyMatch(e -> e.contains("already exist in database"));
            verify(studentRepository, never()).saveAll(anyList());
        }

        @Test
        void importStudentsFromCsv_withMappingErrors_returnsFailure() {
            // Given
            StudentCsvRow csvRow = StudentCsvRow.builder().id("std_001").fullName("A").gender("invalid").build();

            when(csvParserService.parseStudentCsv(any()))
                    .thenReturn(new CsvParserService.ParseResult(List.of(csvRow), new ArrayList<>()));
            when(studentMapper.toEntity(eq(csvRow), anyInt(), anyList())).thenAnswer(invocation -> {
                List<String> errors = invocation.getArgument(2);
                errors.add("Row 2: Invalid gender value");
                return null;
            });

            // When
            StudentImportResponseDto result = studentImportManagement.importStudentsFromCsv(validFile);

            // Then
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getErrors()).anyMatch(e -> e.contains("Invalid gender value"));
            verify(studentRepository, never()).saveAll(anyList());
        }

        @Test
        void importStudentsFromCsv_withDatabaseError_returnsFailure() {
            // Given
            StudentCsvRow csvRow = StudentCsvRow.builder().id("std_001").fullName("A").build();
            Student student = Student.builder().id("std_001").fullName("A").build();

            when(csvParserService.parseStudentCsv(any()))
                    .thenReturn(new CsvParserService.ParseResult(List.of(csvRow), new ArrayList<>()));
            when(studentMapper.toEntity(eq(csvRow), anyInt(), anyList())).thenReturn(student);
            when(studentRepository.findByIdIn(anySet())).thenReturn(Collections.emptyList());
            when(studentRepository.saveAll(anyList())).thenThrow(new RuntimeException("DB Error"));

            // When
            StudentImportResponseDto result = studentImportManagement.importStudentsFromCsv(validFile);

            // Then
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getErrors()).anyMatch(e -> e.contains("Database error"));
        }
    }
}
