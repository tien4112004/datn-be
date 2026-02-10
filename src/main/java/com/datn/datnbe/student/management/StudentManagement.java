package com.datn.datnbe.student.management;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.datn.datnbe.auth.api.ClassGroupApi;
import com.datn.datnbe.auth.api.UserProfileApi;
import com.datn.datnbe.auth.dto.request.SignupRequest;
import com.datn.datnbe.auth.dto.request.UserProfileUpdateRequest;
import com.datn.datnbe.auth.dto.response.UserProfileResponse;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginationDto;
import com.datn.datnbe.sharedkernel.exceptions.ResourceNotFoundException;
import com.datn.datnbe.student.api.StudentApi;
import com.datn.datnbe.student.dto.request.StudentCreateRequest;
import com.datn.datnbe.student.dto.request.StudentUpdateRequest;
import com.datn.datnbe.student.dto.response.StudentResponseDto;
import com.datn.datnbe.student.entity.ClassEnrollment;
import com.datn.datnbe.student.entity.Student;
import com.datn.datnbe.student.enums.EnrollmentStatus;
import com.datn.datnbe.student.mapper.StudentEntityMapper;
import com.datn.datnbe.student.repository.ClassEnrollmentRepository;
import com.datn.datnbe.student.repository.StudentRepository;
import com.datn.datnbe.student.utils.StudentCredentialGenerator;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 * Management service for student CRUD operations.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class StudentManagement implements StudentApi {

    StudentRepository studentRepository;
    ClassEnrollmentRepository classEnrollmentRepository;
    StudentEntityMapper studentEntityMapper;
    UserProfileApi userProfileApi;
    ClassGroupApi classGroupApi;

    @Override
    public StudentResponseDto getStudentById(String id) {
        log.info("Getting student by ID: {}", id);
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + id));
        StudentResponseDto dto = studentEntityMapper.toResponseDto(student);
        enrichWithUserProfile(dto, student.getUserId());
        return dto;
    }

    @Override
    @Transactional
    public StudentResponseDto createStudent(StudentCreateRequest request) {
        log.info("Creating new student with full name: {}", request.getFullName());

        // Phase 1: Create user via UserProfileAPI
        String username = StudentCredentialGenerator
                .generateUsername(request.getFullName(), request.getDateOfBirth(), studentRepository);
        String password = StudentCredentialGenerator.generatePassword();

        // Parse fullName into firstName and lastName
        String[] names = request.getFullName().trim().split("\\s+", 2);
        String firstName = names[0];
        String lastName = names.length > 1 ? names[1] : firstName;

        SignupRequest signupRequest = SignupRequest.builder()
                .username(username)
                .password(password)
                .firstName(firstName)
                .lastName(lastName)
                .role("student")
                .dateOfBirth(request.getDateOfBirth())
                .build();

        UserProfileResponse createdUser = userProfileApi.createUserProfileByUsername(signupRequest);
        String userId = createdUser.getId();

        log.info("User created via UserProfileAPI with ID: {}", userId);

        // Phase 2: Create student entity linked to the user
        Student student = Student.builder()
                .userId(userId)
                .enrollmentDate(new java.util.Date())
                .address(request.getAddress())
                .parentContactEmail(request.getParentContactEmail())
                .gender(request.getGender())
                .parentName(request.getParentName())
                .parentPhone(request.getParentPhone())
                .build();

        Student savedStudent = studentRepository.save(student);

        log.info("Successfully created student with ID: {}", savedStudent.getId());

        // Build response with credentials
        StudentResponseDto response = studentEntityMapper.toResponseDto(savedStudent);
        response.setUsername(username);
        response.setPassword(password);
        // populate profile fields from created user
        try {
            var profile = userProfileApi.getUserProfile(createdUser.getId());
            if (profile != null) {
                response.setFirstName(profile.getFirstName());
                response.setLastName(profile.getLastName());
                response.setAvatarUrl(profile.getAvatarUrl());
                response.setPhoneNumber(profile.getPhoneNumber());
            }
        } catch (Exception e) {
            log.debug("Unable to fetch user profile for created student: {}", e.getMessage());
        }

        return response;
    }

    @Override
    @Transactional
    public StudentResponseDto updateStudent(String id, StudentUpdateRequest request) {
        log.info("Updating student with ID: {}", id);

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + id));

        // Update Student entity fields
        studentEntityMapper.updateEntityFromRequest(student, request);
        Student savedStudent = studentRepository.save(student);

        // Update UserProfile fields if provided
        if (request.getDateOfBirth() != null || request.getPhoneNumber() != null || request.getFullName() != null) {

            var builder = UserProfileUpdateRequest.builder()
                    .dateOfBirth(request.getDateOfBirth())
                    .phoneNumber(request.getPhoneNumber());

            if (request.getFullName() != null) {
                String[] names = request.getFullName().trim().split("\\s+", 2);
                String firstName = names[0];
                String lastName = names.length > 1 ? names[1] : firstName;
                builder.firstName(firstName).lastName(lastName);
            }

            UserProfileUpdateRequest profileUpdateRequest = builder.build();
            try {
                userProfileApi.updateUserProfile(savedStudent.getUserId(), profileUpdateRequest);
            } catch (Exception e) {
                log.error("Failed to update user profile for student {}: {}", id, e.getMessage());
            }
        }

        log.info("Successfully updated student with ID: {}", id);
        StudentResponseDto dto = studentEntityMapper.toResponseDto(savedStudent);
        enrichWithUserProfile(dto, savedStudent.getUserId());
        return dto;
    }

    @Override
    @Transactional
    public void deleteStudent(String id) {
        log.info("Deleting student with ID: {}", id);

        if (!studentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Student not found with ID: " + id);
        }

        // Also remove from all class enrollments
        classEnrollmentRepository.deleteAllInBatch(classEnrollmentRepository.findAll()
                .stream()
                .filter(e -> e.getStudentId().equals(id))
                .collect(Collectors.toList()));

        studentRepository.deleteById(id);
        log.info("Successfully deleted student with ID: {}", id);
    }

    @Override
    public PaginatedResponseDto<StudentResponseDto> getStudentsByClass(String classId, Pageable pageable) {
        log.info("Getting students for class ID: {} with pagination", classId);

        // Get all enrollments for the class (we'll need to handle pagination
        // differently)
        List<ClassEnrollment> allEnrollments = classEnrollmentRepository.findByClassId(classId);
        List<String> studentIds = allEnrollments.stream().map(ClassEnrollment::getStudentId).toList();

        if (studentIds.isEmpty()) {
            return PaginatedResponseDto.<StudentResponseDto>builder()
                    .data(List.of())
                    .pagination(PaginationDto.builder()
                            .currentPage(pageable.getPageNumber())
                            .pageSize(pageable.getPageSize())
                            .totalItems(0L)
                            .totalPages(0)
                            .build())
                    .build();
        }

        // Get students with pagination
        Page<Student> studentsPage = studentRepository.findByIdIn(Set.copyOf(studentIds), pageable);

        List<StudentResponseDto> studentDtos = studentsPage.getContent().stream().map(s -> {
            StudentResponseDto dto = studentEntityMapper.toResponseDto(s);
            enrichWithUserProfile(dto, s.getUserId());
            return dto;
        }).collect(Collectors.toList());

        var paginationDto = PaginationDto.getFromPageable(pageable);
        paginationDto.setTotalItems(studentsPage.getTotalElements());
        paginationDto.setTotalPages(studentsPage.getTotalPages());

        return PaginatedResponseDto.<StudentResponseDto>builder().data(studentDtos).pagination(paginationDto).build();
    }

    @Override
    @Transactional
    public StudentResponseDto enrollStudent(String classId, String studentId) {
        log.info("Enrolling student {} to class {}", studentId, classId);

        // Check if student exists
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));

        // Check if already enrolled
        if (classEnrollmentRepository.existsByClassIdAndStudentId(classId, studentId)) {
            log.warn("Student {} is already enrolled in class {}", studentId, classId);
            return studentEntityMapper.toResponseDto(student);
        }

        // Create enrollment
        ClassEnrollment enrollment = ClassEnrollment.builder()
                .classId(classId)
                .studentId(studentId)
                .status(EnrollmentStatus.ACTIVE)
                .build();

        classEnrollmentRepository.save(enrollment);
        log.info("Successfully enrolled student {} to class {}", studentId, classId);

        // Add student to class's Keycloak group for resource access
        String keycloakUserId = userProfileApi.getKeycloakUserIdByUserId(student.getUserId());
        if (keycloakUserId != null) {
            classGroupApi.addUserToClassGroup(classId, keycloakUserId);
        } else {
            log.warn("Could not find Keycloak user ID for student {}, skipping group assignment", studentId);
        }

        StudentResponseDto dto = studentEntityMapper.toResponseDto(student);
        enrichWithUserProfile(dto, student.getUserId());
        return dto;
    }

    private void enrichWithUserProfile(StudentResponseDto dto, String userId) {
        if (dto == null || userId == null)
            return;
        try {
            UserProfileResponse profile = userProfileApi.getUserProfile(userId);
            if (profile != null) {
                dto.setUsername(profile.getEmail());
                dto.setFirstName(profile.getFirstName());
                dto.setLastName(profile.getLastName());
                dto.setAvatarUrl(profile.getAvatarUrl());
                dto.setPhoneNumber(profile.getPhoneNumber());
            }
        } catch (Exception ex) {
            log.debug("Failed to enrich student DTO with user profile for userId {}: {}", userId, ex.getMessage());
        }
    }

    @Override
    @Transactional
    public void removeStudentFromClass(String classId, String studentId) {
        log.info("Removing student {} from class {}", studentId, classId);

        // Check if enrollment exists
        if (!classEnrollmentRepository.existsByClassIdAndStudentId(classId, studentId)) {
            throw new ResourceNotFoundException("Student " + studentId + " is not enrolled in class " + classId);
        }

        // Get student info before deletion for Keycloak group removal
        Student student = studentRepository.findById(studentId).orElse(null);

        classEnrollmentRepository.deleteByClassIdAndStudentId(classId, studentId);

        // Remove student from class's Keycloak group
        if (student != null && student.getUserId() != null) {
            String keycloakUserId = userProfileApi.getKeycloakUserIdByUserId(student.getUserId());
            if (keycloakUserId != null) {
                classGroupApi.removeUserFromClassGroup(classId, keycloakUserId);
            } else {
                log.warn("Could not find Keycloak user ID for student {}, skipping group removal", studentId);
            }
        }

        log.info("Successfully removed student {} from class {}", studentId, classId);
    }

    @Override
    @Transactional
    public List<StudentResponseDto> regeneratePasswords(List<String> studentIds) {
        log.info("Regenerating passwords for {} students", studentIds.size());

        List<StudentResponseDto> responses = new java.util.ArrayList<>();

        for (String studentId : studentIds) {
            try {
                Student student = studentRepository.findById(studentId)
                        .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));

                // Generate new password
                String newPassword = StudentCredentialGenerator.generatePassword();

                // Update password via UserProfileAPI
                userProfileApi.updatePassword(student.getUserId(), newPassword);

                log.info("Password regenerated for student {} (user {})", studentId, student.getUserId());

                // Build response with new password
                StudentResponseDto response = studentEntityMapper.toResponseDto(student);
                response.setPassword(newPassword);
                enrichWithUserProfile(response, student.getUserId());
                responses.add(response);

            } catch (Exception e) {
                log.error("Failed to regenerate password for student {}: {}", studentId, e.getMessage());
                throw new RuntimeException("Failed to regenerate password for student " + studentId, e);
            }
        }

        return responses;
    }

    @Override
    public Optional<String> getKeycloakUserIdForStudent(String studentId) {
        return studentRepository.findById(studentId)
                .map(Student::getUserId)
                .map(userProfileApi::getKeycloakUserIdByUserId);
    }

    @Override
    public List<String> getEnrolledStudentKeycloakUserIds(String classId) {
        List<ClassEnrollment> enrollments = classEnrollmentRepository.findByClassId(classId);
        List<String> keycloakUserIds = new ArrayList<>();

        for (ClassEnrollment enrollment : enrollments) {
            Optional<String> keycloakUserId = getKeycloakUserIdForStudent(enrollment.getStudentId());
            keycloakUserId.ifPresent(keycloakUserIds::add);
        }

        return keycloakUserIds;
    }

    @Override
    public List<StudentResponseDto> getStudentsByClassId(String classId) {
        return studentRepository.findByClassId(classId).stream().map(studentEntityMapper::toResponseDto).toList();
    }
}
