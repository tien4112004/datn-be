package com.datn.datnbe.student.management;

import com.datn.datnbe.student.dto.request.StudentCsvRow;
import com.datn.datnbe.student.dto.response.StudentImportResponseDto;
import com.datn.datnbe.student.entity.Student;
import com.datn.datnbe.student.enums.Role;
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
        String csvContent = "firstName,lastName,email\nNguyen,Van A,student@example.com";
        validFile = new MockMultipartFile("file", "students.csv", "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8));
    }

    @Nested
    @DisplayName("Successful import tests")
    class SuccessfulImportTests {

        @Test
        void importStudentsFromCsv_withValidData_returnsSuccess() {
            StudentCsvRow csvRow = StudentCsvRow.builder()
                    .firstName("Nguyen")
                    .lastName("Van A")
                    .email("student@example.com")
                    .build();
            Student student = Student.builder()
                    .id("550e8400-e29b-41d4-a716-446655440002")
                    .firstName("Nguyen")
                    .lastName("Van A")
                    .email("student@example.com")
                    .role(Role.STUDENT)
                    .build();

            when(csvParserService.parseStudentCsv(any()))
                    .thenReturn(new CsvParserService.ParseResult(List.of(csvRow), new ArrayList<>()));
            when(studentMapper.toEntity(eq(csvRow), anyInt(), anyList())).thenReturn(student);
            when(studentRepository.existsByEmail(anyString())).thenReturn(false);
            when(studentRepository.saveAll(anyList())).thenReturn(List.of(student));

            StudentImportResponseDto result = studentImportManagement.importStudentsFromCsv(validFile);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getStudentsCreated()).isEqualTo(1);
            assertThat(result.getErrors()).isEmpty();
            verify(studentRepository).saveAll(anyList());
        }

        @Test
        void importStudentsFromCsv_withMultipleStudents_savesAll() {
            StudentCsvRow csvRow1 = StudentCsvRow.builder().firstName("A").lastName("A").email("a@example.com").build();
            StudentCsvRow csvRow2 = StudentCsvRow.builder().firstName("B").lastName("B").email("b@example.com").build();
            Student student1 = Student.builder()
                    .id("id1")
                    .firstName("A")
                    .lastName("A")
                    .email("a@example.com")
                    .role(Role.STUDENT)
                    .build();
            Student student2 = Student.builder()
                    .id("id2")
                    .firstName("B")
                    .lastName("B")
                    .email("b@example.com")
                    .role(Role.STUDENT)
                    .build();

            when(csvParserService.parseStudentCsv(any()))
                    .thenReturn(new CsvParserService.ParseResult(List.of(csvRow1, csvRow2), new ArrayList<>()));
            when(studentMapper.toEntity(eq(csvRow1), anyInt(), anyList())).thenReturn(student1);
            when(studentMapper.toEntity(eq(csvRow2), anyInt(), anyList())).thenReturn(student2);
            when(studentRepository.existsByEmail(anyString())).thenReturn(false);
            when(studentRepository.saveAll(anyList())).thenReturn(List.of(student1, student2));

            StudentImportResponseDto result = studentImportManagement.importStudentsFromCsv(validFile);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getStudentsCreated()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Failed import tests")
    class FailedImportTests {

        @Test
        void importStudentsFromCsv_withParseErrors_returnsFailure() {
            List<String> parseErrors = List.of("Row 2: firstName is required");
            when(csvParserService.parseStudentCsv(any()))
                    .thenReturn(new CsvParserService.ParseResult(new ArrayList<>(), new ArrayList<>(parseErrors)));

            StudentImportResponseDto result = studentImportManagement.importStudentsFromCsv(validFile);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getStudentsCreated()).isEqualTo(0);
            assertThat(result.getErrors()).contains("Row 2: firstName is required");
            verify(studentRepository, never()).saveAll(anyList());
        }

        @Test
        void importStudentsFromCsv_withDuplicateEmailsInCsv_returnsFailure() {
            StudentCsvRow csvRow1 = StudentCsvRow.builder()
                    .firstName("A")
                    .lastName("A")
                    .email("duplicate@example.com")
                    .build();
            StudentCsvRow csvRow2 = StudentCsvRow.builder()
                    .firstName("B")
                    .lastName("B")
                    .email("duplicate@example.com")
                    .build();
            Student student1 = Student.builder()
                    .id("id1")
                    .firstName("A")
                    .lastName("A")
                    .email("duplicate@example.com")
                    .role(Role.STUDENT)
                    .build();
            Student student2 = Student.builder()
                    .id("id2")
                    .firstName("B")
                    .lastName("B")
                    .email("duplicate@example.com")
                    .role(Role.STUDENT)
                    .build();

            when(csvParserService.parseStudentCsv(any()))
                    .thenReturn(new CsvParserService.ParseResult(List.of(csvRow1, csvRow2), new ArrayList<>()));
            when(studentMapper.toEntity(eq(csvRow1), anyInt(), anyList())).thenReturn(student1);
            when(studentMapper.toEntity(eq(csvRow2), anyInt(), anyList())).thenReturn(student2);

            StudentImportResponseDto result = studentImportManagement.importStudentsFromCsv(validFile);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getErrors()).anyMatch(e -> e.contains("Duplicate emails found in CSV"));
            verify(studentRepository, never()).saveAll(anyList());
        }

        @Test
        void importStudentsFromCsv_withExistingEmailsInDb_returnsFailure() {
            StudentCsvRow csvRow = StudentCsvRow.builder()
                    .firstName("A")
                    .lastName("A")
                    .email("existing@example.com")
                    .build();
            Student student = Student.builder()
                    .id("id1")
                    .firstName("A")
                    .lastName("A")
                    .email("existing@example.com")
                    .role(Role.STUDENT)
                    .build();

            when(csvParserService.parseStudentCsv(any()))
                    .thenReturn(new CsvParserService.ParseResult(List.of(csvRow), new ArrayList<>()));
            when(studentMapper.toEntity(eq(csvRow), anyInt(), anyList())).thenReturn(student);
            when(studentRepository.existsByEmail("existing@example.com")).thenReturn(true);

            StudentImportResponseDto result = studentImportManagement.importStudentsFromCsv(validFile);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getErrors()).anyMatch(e -> e.contains("Email already exists in database"));
            verify(studentRepository, never()).saveAll(anyList());
        }

        @Test
        void importStudentsFromCsv_withMappingErrors_returnsFailure() {
            StudentCsvRow csvRow = StudentCsvRow.builder().firstName("A").lastName("A").email("invalid-email").build();

            when(csvParserService.parseStudentCsv(any()))
                    .thenReturn(new CsvParserService.ParseResult(List.of(csvRow), new ArrayList<>()));
            when(studentMapper.toEntity(eq(csvRow), anyInt(), anyList())).thenAnswer(invocation -> {
                List<String> errors = invocation.getArgument(2);
                errors.add("Row 2: Invalid email format");
                return null;
            });

            StudentImportResponseDto result = studentImportManagement.importStudentsFromCsv(validFile);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getErrors()).anyMatch(e -> e.contains("Invalid email format"));
            verify(studentRepository, never()).saveAll(anyList());
        }

        @Test
        void importStudentsFromCsv_withDatabaseError_returnsFailure() {
            StudentCsvRow csvRow = StudentCsvRow.builder().firstName("A").lastName("A").email("a@example.com").build();
            Student student = Student.builder()
                    .id("id1")
                    .firstName("A")
                    .lastName("A")
                    .email("a@example.com")
                    .role(Role.STUDENT)
                    .build();

            when(csvParserService.parseStudentCsv(any()))
                    .thenReturn(new CsvParserService.ParseResult(List.of(csvRow), new ArrayList<>()));
            when(studentMapper.toEntity(eq(csvRow), anyInt(), anyList())).thenReturn(student);
            when(studentRepository.existsByEmail(anyString())).thenReturn(false);
            when(studentRepository.saveAll(anyList())).thenThrow(new RuntimeException("DB Error"));

            StudentImportResponseDto result = studentImportManagement.importStudentsFromCsv(validFile);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getErrors()).anyMatch(e -> e.contains("Database error"));
        }
    }
}
