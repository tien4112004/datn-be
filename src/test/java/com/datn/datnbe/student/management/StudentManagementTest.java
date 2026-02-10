package com.datn.datnbe.student.management;

import com.datn.datnbe.student.dto.request.StudentUpdateRequest;
import com.datn.datnbe.student.dto.response.StudentResponseDto;
import com.datn.datnbe.student.entity.Student;
import com.datn.datnbe.student.mapper.StudentEntityMapper;
import com.datn.datnbe.student.repository.ClassEnrollmentRepository;
import com.datn.datnbe.student.repository.StudentRepository;
import com.datn.datnbe.auth.api.UserProfileApi;
import com.datn.datnbe.sharedkernel.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;
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
    private ClassEnrollmentRepository classEnrollmentRepository;

    @Mock
    private StudentEntityMapper studentEntityMapper;

    @Mock
    private UserProfileApi userProfileApi;

    @InjectMocks
    private StudentManagement studentManagement;

    private Student testStudent;
    private StudentResponseDto testResponseDto;

    @BeforeEach
    void setUp() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        testStudent = Student.builder()
                .id("std_001")
                .userId("user_001")
                .enrollmentDate(sdf.parse("2024-01-15"))
                .address("123 Main St")
                .parentContactEmail("parent@example.com")
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();

        testResponseDto = StudentResponseDto.builder()
                .id("std_001")
                .userId("user_001")
                .enrollmentDate(sdf.parse("2024-01-15"))
                .address("123 Main St")
                .parentContactEmail("parent@example.com")
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

            // mock user profile enrichment
            var profile = com.datn.datnbe.auth.dto.response.UserProfileResponse.builder()
                .id("user_001")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .avatarUrl("https://cdn/avatar.png")
                .phoneNumber("0123456789")
                .build();
            when(userProfileApi.getUserProfile("user_001")).thenReturn(profile);

            // When
            StudentResponseDto result = studentManagement.getStudentById("std_001");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("std_001");
            assertThat(result.getUserId()).isEqualTo("user_001");
            verify(studentRepository).findById("std_001");
            verify(userProfileApi).getUserProfile("user_001");
            assertThat(result.getFirstName()).isEqualTo("John");
            assertThat(result.getLastName()).isEqualTo("Doe");
            assertThat(result.getAvatarUrl()).isEqualTo("https://cdn/avatar.png");
            assertThat(result.getPhoneNumber()).isEqualTo("0123456789");
            // username is populated from the user's email in the current implementation
            assertThat(result.getUsername()).isEqualTo("john.doe@example.com");
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
            StudentUpdateRequest request = StudentUpdateRequest.builder().address("456 New St").build();

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
            StudentUpdateRequest request = StudentUpdateRequest.builder().build();

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
            when(classEnrollmentRepository.findAll()).thenReturn(java.util.Collections.emptyList());
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
