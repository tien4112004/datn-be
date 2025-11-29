package com.datn.datnbe.student.management;

import com.datn.datnbe.student.dto.request.StudentUpdateRequest;
import com.datn.datnbe.student.dto.response.StudentResponseDto;
import com.datn.datnbe.student.entity.Student;
import com.datn.datnbe.student.enums.Gender;
import com.datn.datnbe.student.enums.StudentStatus;
import com.datn.datnbe.student.mapper.StudentEntityMapper;
import com.datn.datnbe.student.repository.StudentRepository;
import com.datn.datnbe.sharedkernel.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentManagementTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private StudentEntityMapper studentEntityMapper;

    @InjectMocks
    private StudentManagement studentManagement;

    private Student testStudent;
    private StudentResponseDto testResponseDto;

    @BeforeEach
    void setUp() {
        testStudent = Student.builder()
                .id("std_001")
                .fullName("Nguyen Van A")
                .dateOfBirth(LocalDate.of(2008, 1, 15))
                .gender(Gender.MALE)
                .address("123 Main St")
                .parentName("Nguyen Van X")
                .parentPhone("+84987654321")
                .classId("cls_001")
                .enrollmentDate(LocalDate.of(2024, 1, 15))
                .status(StudentStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testResponseDto = StudentResponseDto.builder()
                .id("std_001")
                .fullName("Nguyen Van A")
                .dateOfBirth(LocalDate.of(2008, 1, 15))
                .gender(Gender.MALE)
                .address("123 Main St")
                .parentName("Nguyen Van X")
                .parentPhone("+84987654321")
                .classId("cls_001")
                .enrollmentDate(LocalDate.of(2024, 1, 15))
                .status(StudentStatus.ACTIVE)
                .build();
    }

    @Nested
    @DisplayName("Get Student By ID Tests")
    class GetStudentByIdTests {

        @Test
        void getStudentById_withExistingId_returnsStudent() {
            // Given
            when(studentRepository.findById("std_001")).thenReturn(Optional.of(testStudent));
            when(studentEntityMapper.toResponseDto(testStudent)).thenReturn(testResponseDto);

            // When
            StudentResponseDto result = studentManagement.getStudentById("std_001");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("std_001");
            assertThat(result.getFullName()).isEqualTo("Nguyen Van A");
            verify(studentRepository).findById("std_001");
        }

        @Test
        void getStudentById_withNonExistentId_throwsException() {
            // Given
            when(studentRepository.findById("non_existent")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> studentManagement.getStudentById("non_existent"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Student not found");
        }
    }

    @Nested
    @DisplayName("Update Student Tests")
    class UpdateStudentTests {

        @Test
        void updateStudent_withValidRequest_returnsUpdatedStudent() {
            // Given
            StudentUpdateRequest request = StudentUpdateRequest.builder()
                    .fullName("Nguyen Van B")
                    .status(StudentStatus.INACTIVE)
                    .build();

            when(studentRepository.findById("std_001")).thenReturn(Optional.of(testStudent));
            when(studentRepository.save(any(Student.class))).thenReturn(testStudent);
            when(studentEntityMapper.toResponseDto(testStudent)).thenReturn(testResponseDto);

            // When
            StudentResponseDto result = studentManagement.updateStudent("std_001", request);

            // Then
            assertThat(result).isNotNull();
            verify(studentRepository).findById("std_001");
            verify(studentEntityMapper).updateEntityFromRequest(testStudent, request);
            verify(studentRepository).save(testStudent);
        }

        @Test
        void updateStudent_withNonExistentId_throwsException() {
            // Given
            StudentUpdateRequest request = StudentUpdateRequest.builder().fullName("Nguyen Van B").build();

            when(studentRepository.findById("non_existent")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> studentManagement.updateStudent("non_existent", request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Student not found");

            verify(studentRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Delete Student Tests")
    class DeleteStudentTests {

        @Test
        void deleteStudent_withExistingId_deletesSuccessfully() {
            // Given
            when(studentRepository.existsById("std_001")).thenReturn(true);
            doNothing().when(studentRepository).deleteById("std_001");

            // When
            studentManagement.deleteStudent("std_001");

            // Then
            verify(studentRepository).existsById("std_001");
            verify(studentRepository).deleteById("std_001");
        }

        @Test
        void deleteStudent_withNonExistentId_throwsException() {
            // Given
            when(studentRepository.existsById("non_existent")).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> studentManagement.deleteStudent("non_existent"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Student not found");

            verify(studentRepository, never()).deleteById(any());
        }
    }
}
