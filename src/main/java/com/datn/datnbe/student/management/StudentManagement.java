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
import com.datn.datnbe.student.utils.StudentCredentialGenerator;
import com.datn.datnbe.sharedkernel.exceptions.ResourceNotFoundException;
import com.datn.datnbe.auth.api.UserProfileApi;
import com.datn.datnbe.auth.dto.request.SignupRequest;
import com.datn.datnbe.auth.dto.response.UserProfileResponse;
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
    UserProfileApi userProfileApi;

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
        log.info("Creating new student with full name: {}", request.getFullName());

        // Phase 1: Create user via UserProfileAPI
        String email = StudentCredentialGenerator.generateEmail(request.getFullName());
        String password = StudentCredentialGenerator.generatePassword();

        // Parse fullName into firstName and lastName
        String[] names = request.getFullName().trim().split("\\s+", 2);
        String firstName = names[0];
        String lastName = names.length > 1 ? names[1] : firstName;

        SignupRequest signupRequest = SignupRequest.builder()
                .email(email)
                .password(password)
                .firstName(firstName)
                .lastName(lastName)
                .build();

        UserProfileResponse createdUser = userProfileApi.createUserProfile(signupRequest);
        String userId = createdUser.getId();

        log.info("User created via UserProfileAPI with ID: {}", userId);

        // Phase 2: Create student entity linked to the user
        Student student = Student.builder()
                .userId(userId)
                .enrollmentDate(request.getEnrollmentDate())
                .address(request.getAddress())
                .parentContactEmail(request.getParentContactEmail())
                .build();

        Student savedStudent = studentRepository.save(student);

        log.info("Successfully created student with ID: {}", savedStudent.getId());
        
        // Build response with credentials
        StudentResponseDto response = studentEntityMapper.toResponseDto(savedStudent);
        response.setUsername(email);
        response.setPassword(password);
        response.setEmail(email);
        
        return response;
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
    public List<StudentResponseDto> getStudentsByClass(String classId) {
        log.info("Getting students for class ID: {}", classId);

        List<ClassEnrollment> enrollments = classEnrollmentRepository.findByClassId(classId);
        List<String> studentIds = enrollments.stream().map(ClassEnrollment::getStudentId).collect(Collectors.toList());

        if (studentIds.isEmpty()) {
            return List.of();
        }

        List<Student> students = studentRepository.findByIdIn(Set.copyOf(studentIds));

        return students.stream().map(studentEntityMapper::toResponseDto).collect(Collectors.toList());
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

        return studentEntityMapper.toResponseDto(student);
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
