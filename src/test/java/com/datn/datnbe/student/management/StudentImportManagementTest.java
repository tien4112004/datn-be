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
import com.datn.datnbe.student.api.StudentApi;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
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

    @Mock
    private UserProfileApi userProfileApi;

    @Mock
    private StudentApi studentApi;

    @InjectMocks
    private StudentImportManagement studentImportManagement;

    private MockMultipartFile validFile;

    @BeforeEach
    void setUp() {
        String csvContent = "fullName,dateOfBirth,gender,address,parentName,parentPhone,parentContactEmail,classId\nNguyen Van A,2008-01-15,male,123 Main St,Nguyen Van X,+84987654321,parent@example.com,cls_001";
        validFile = new MockMultipartFile("file", "students.csv", "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8));
    }

    @Nested
    @DisplayName("Failed import tests")
    class FailedImportTests {

        @Test
        void importStudentsFromCsv_withParseErrors_returnsFailure() {
            List<String> parseErrors = List.of("Row 2: fullName is required");
            when(csvParserService.parseStudentCsv(any()))
                    .thenReturn(new CsvParserService.ParseResult(new ArrayList<>(), new ArrayList<>(parseErrors)));

            StudentImportResponseDto result = studentImportManagement.importStudentsFromCsv("cls_001", validFile);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getStudentsCreated()).isEqualTo(0);
            assertThat(result.getErrors()).contains("Row 2: fullName is required");
            assertThat(result.getCredentials()).isEmpty();
            verify(studentRepository, never()).saveAll(anyList());
        }

        @Test
        void importStudentsFromCsv_withDuplicateUserIdsInCsv_returnsFailure() {
            StudentCsvRow csvRow1 = StudentCsvRow.builder()
                    .fullName("Nguyen Van A")
                    .dateOfBirth(Date.from(LocalDate.of(2008, 1, 15).atStartOfDay(ZoneId.systemDefault()).toInstant()))
                    .gender("male")
                    .classId("cls_001")
                    .build();
            StudentCsvRow csvRow2 = StudentCsvRow.builder()
                    .fullName("Tran Thi B")
                    .dateOfBirth(Date.from(LocalDate.of(2008, 5, 20).atStartOfDay(ZoneId.systemDefault()).toInstant()))
                    .gender("female")
                    .classId("cls_001")
                    .build();

            UserProfileResponse createdUser = UserProfileResponse.builder()
                    .id("user_001")
                    .email("test@example.com")
                    .firstName("Test")
                    .lastName("User")
                    .build();

            Student student1 = Student.builder().userId("user_001").build();
            Student student2 = Student.builder().userId("user_001").build();

            when(csvParserService.parseStudentCsv(any()))
                    .thenReturn(new CsvParserService.ParseResult(List.of(csvRow1, csvRow2), new ArrayList<>()));
            // These stubbings may not be exercised if duplicate user ids are detected early
            lenient().when(userProfileApi.createUserProfile(any())).thenReturn(createdUser);
            lenient().when(studentMapper.toEntity(eq(csvRow1), anyInt(), anyList())).thenReturn(student1);
            lenient().when(studentMapper.toEntity(eq(csvRow2), anyInt(), anyList())).thenReturn(student2);

            StudentImportResponseDto result = studentImportManagement.importStudentsFromCsv("cls_001", validFile);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getErrors()).anyMatch(e -> e.contains("Duplicate user IDs found in CSV"));
            assertThat(result.getCredentials()).isEmpty();
            verify(studentRepository, never()).saveAll(anyList());
        }

        @Test
        void importStudentsFromCsv_withMappingErrors_returnsFailure() {
            StudentCsvRow csvRow = StudentCsvRow.builder().fullName("Test Student").build();

            UserProfileResponse createdUser = UserProfileResponse.builder()
                    .id("user_001")
                    .email("test.student@example.com")
                    .firstName("Test")
                    .lastName("Student")
                    .build();

            when(csvParserService.parseStudentCsv(any()))
                    .thenReturn(new CsvParserService.ParseResult(List.of(csvRow), new ArrayList<>()));
            when(userProfileApi.createUserProfile(any())).thenReturn(createdUser);
            when(studentMapper.toEntity(eq(csvRow), anyInt(), anyList())).thenAnswer(invocation -> {
                List<String> errors = invocation.getArgument(2);
                errors.add("Row 2: userId is required");
                return null;
            });

            StudentImportResponseDto result = studentImportManagement.importStudentsFromCsv("cls_001", validFile);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getErrors()).anyMatch(e -> e.contains("userId is required"));
            assertThat(result.getCredentials()).isEmpty();
            verify(studentRepository, never()).saveAll(anyList());
        }

    }
}
