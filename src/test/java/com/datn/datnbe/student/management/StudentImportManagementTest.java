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
import java.time.LocalDate;
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
        String csvContent = "userId,enrollmentDate,address,parentContactEmail\nuser_001,2024-01-15,123 Main St,parent@example.com";
        validFile = new MockMultipartFile("file", "students.csv", "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8));
    }

    @Nested
    @DisplayName("Successful import tests")
    class SuccessfulImportTests {

        @Test
        void importStudentsFromCsv_withValidData_returnsSuccess() {
            StudentCsvRow csvRow = StudentCsvRow.builder()
                    .userId("user_001")
                    .enrollmentDate(LocalDate.of(2024, 1, 15))
                    .address("123 Main St")
                    .parentContactEmail("parent@example.com")
                    .build();
            Student student = Student.builder()
                    .userId("user_001")
                    .enrollmentDate(LocalDate.of(2024, 1, 15))
                    .address("123 Main St")
                    .parentContactEmail("parent@example.com")
                    .build();

            when(csvParserService.parseStudentCsv(any()))
                    .thenReturn(new CsvParserService.ParseResult(List.of(csvRow), new ArrayList<>()));
            when(studentMapper.toEntity(eq(csvRow), anyInt(), anyList())).thenReturn(student);
            when(studentRepository.existsByUserId(anyString())).thenReturn(false);
            when(studentRepository.saveAll(anyList())).thenReturn(List.of(student));

            StudentImportResponseDto result = studentImportManagement.importStudentsFromCsv(validFile);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getStudentsCreated()).isEqualTo(1);
            assertThat(result.getErrors()).isEmpty();
            verify(studentRepository).saveAll(anyList());
        }

        @Test
        void importStudentsFromCsv_withMultipleStudents_savesAll() {
            StudentCsvRow csvRow1 = StudentCsvRow.builder()
                    .userId("user_001")
                    .enrollmentDate(LocalDate.of(2024, 1, 15))
                    .build();
            StudentCsvRow csvRow2 = StudentCsvRow.builder()
                    .userId("user_002")
                    .enrollmentDate(LocalDate.of(2024, 1, 20))
                    .build();
            Student student1 = Student.builder().userId("user_001").enrollmentDate(LocalDate.of(2024, 1, 15)).build();
            Student student2 = Student.builder().userId("user_002").enrollmentDate(LocalDate.of(2024, 1, 20)).build();

            when(csvParserService.parseStudentCsv(any()))
                    .thenReturn(new CsvParserService.ParseResult(List.of(csvRow1, csvRow2), new ArrayList<>()));
            when(userProfileApi.createUserProfile(any())).thenReturn(createdUser1).thenReturn(createdUser2);
            when(studentMapper.toEntity(eq(csvRow1), anyInt(), anyList())).thenReturn(student1);
            when(studentMapper.toEntity(eq(csvRow2), anyInt(), anyList())).thenReturn(student2);
            when(studentRepository.existsByUserId(anyString())).thenReturn(false);
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
            List<String> parseErrors = List.of("Row 2: userId is required");
            when(csvParserService.parseStudentCsv(any()))
                    .thenReturn(new CsvParserService.ParseResult(new ArrayList<>(), new ArrayList<>(parseErrors)));

            StudentImportResponseDto result = studentImportManagement.importStudentsFromCsv(validFile);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getStudentsCreated()).isEqualTo(0);
            assertThat(result.getErrors()).contains("Row 2: userId is required");
            verify(studentRepository, never()).saveAll(anyList());
        }

        @Test
        void importStudentsFromCsv_withDuplicateUserIdsInCsv_returnsFailure() {
            StudentCsvRow csvRow1 = StudentCsvRow.builder()
                    .userId("user_001")
                    .enrollmentDate(LocalDate.of(2024, 1, 15))
                    .build();
            StudentCsvRow csvRow2 = StudentCsvRow.builder()
                    .userId("user_001")
                    .enrollmentDate(LocalDate.of(2024, 1, 20))
                    .build();
            Student student1 = Student.builder().userId("user_001").enrollmentDate(LocalDate.of(2024, 1, 15)).build();
            Student student2 = Student.builder().userId("user_001").enrollmentDate(LocalDate.of(2024, 1, 20)).build();

            when(csvParserService.parseStudentCsv(any()))
                    .thenReturn(new CsvParserService.ParseResult(List.of(csvRow1, csvRow2), new ArrayList<>()));
            when(studentMapper.toEntity(eq(csvRow1), anyInt(), anyList())).thenReturn(student1);
            when(studentMapper.toEntity(eq(csvRow2), anyInt(), anyList())).thenReturn(student2);

            StudentImportResponseDto result = studentImportManagement.importStudentsFromCsv(validFile);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getErrors()).anyMatch(e -> e.contains("Duplicate user IDs found in CSV"));
            verify(studentRepository, never()).saveAll(anyList());
        }

        @Test
        void importStudentsFromCsv_withExistingUserIdsInDb_returnsFailure() {
            StudentCsvRow csvRow = StudentCsvRow.builder()
                    .userId("user_001")
                    .enrollmentDate(LocalDate.of(2024, 1, 15))
                    .build();
            Student student = Student.builder().userId("user_001").enrollmentDate(LocalDate.of(2024, 1, 15)).build();

            when(csvParserService.parseStudentCsv(any()))
                    .thenReturn(new CsvParserService.ParseResult(List.of(csvRow), new ArrayList<>()));
            when(studentMapper.toEntity(eq(csvRow), anyInt(), anyList())).thenReturn(student);
            when(studentRepository.existsByUserId("user_001")).thenReturn(true);

            StudentImportResponseDto result = studentImportManagement.importStudentsFromCsv(validFile);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getErrors()).anyMatch(e -> e.contains("Student already exists for user ID"));
            verify(studentRepository, never()).saveAll(anyList());
        }

        @Test
        void importStudentsFromCsv_withMappingErrors_returnsFailure() {
            StudentCsvRow csvRow = StudentCsvRow.builder().userId(null).build();

            when(csvParserService.parseStudentCsv(any()))
                    .thenReturn(new CsvParserService.ParseResult(List.of(csvRow), new ArrayList<>()));
            when(studentMapper.toEntity(eq(csvRow), anyInt(), anyList())).thenAnswer(invocation -> {
                List<String> errors = invocation.getArgument(2);
                errors.add("Row 2: userId is required");
                return null;
            });

            StudentImportResponseDto result = studentImportManagement.importStudentsFromCsv(validFile);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getErrors()).anyMatch(e -> e.contains("userId is required"));
            verify(studentRepository, never()).saveAll(anyList());
        }

        @Test
        void importStudentsFromCsv_withDatabaseError_returnsFailure() {
            StudentCsvRow csvRow = StudentCsvRow.builder()
                    .userId("user_001")
                    .enrollmentDate(LocalDate.of(2024, 1, 15))
                    .build();
            Student student = Student.builder().userId("user_001").enrollmentDate(LocalDate.of(2024, 1, 15)).build();

            when(csvParserService.parseStudentCsv(any()))
                    .thenReturn(new CsvParserService.ParseResult(List.of(csvRow), new ArrayList<>()));
            when(studentMapper.toEntity(eq(csvRow), anyInt(), anyList())).thenReturn(student);
            when(studentRepository.existsByUserId(anyString())).thenReturn(false);
            when(studentRepository.saveAll(anyList())).thenThrow(new RuntimeException("DB Error"));

            StudentImportResponseDto result = studentImportManagement.importStudentsFromCsv(validFile);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getErrors()).anyMatch(e -> e.contains("Database error"));
        }
    }
}
