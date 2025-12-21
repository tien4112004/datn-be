package com.datn.datnbe.student.management;

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
import com.datn.datnbe.sharedkernel.exceptions.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Override
    public StudentResponseDto getStudentById(String id) {
        log.info("Getting student by ID: {}", id);
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + id));
        return studentEntityMapper.toResponseDto(student);
    }

    @Override
    @Transactional
    public StudentResponseDto createStudent(StudentCreateRequest request) {
        log.info("Creating new student with user ID: {}", request.getUserId());

        // Check if student already exists for this user
        if (studentRepository.existsByUserId(request.getUserId())) {
            throw new IllegalArgumentException("Student already exists for user ID: " + request.getUserId());
        }

        Student student = studentEntityMapper.toEntity(request);
        Student savedStudent = studentRepository.save(student);

        log.info("Successfully created student with ID: {}", savedStudent.getId());
        return studentEntityMapper.toResponseDto(savedStudent);
    }

    @Override
    @Transactional
    public StudentResponseDto updateStudent(String id, StudentUpdateRequest request) {
        log.info("Updating student with ID: {}", id);

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + id));

        studentEntityMapper.updateEntityFromRequest(student, request);
        Student savedStudent = studentRepository.save(student);

        log.info("Successfully updated student with ID: {}", id);
        return studentEntityMapper.toResponseDto(savedStudent);
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
        List<String> studentIds = allEnrollments.stream()
                .map(ClassEnrollment::getStudentId)
                .collect(Collectors.toList());

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
        org.springframework.data.domain.Page<Student> studentsPage = studentRepository
                .findByIdIn(Set.copyOf(studentIds), pageable);

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

        classEnrollmentRepository.deleteByClassIdAndStudentId(classId, studentId);
        log.info("Successfully removed student {} from class {}", studentId, classId);
    }
}
