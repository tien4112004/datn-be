package com.datn.datnbe.student.management;

import com.datn.datnbe.student.api.StudentApi;
import com.datn.datnbe.student.dto.request.StudentUpdateRequest;
import com.datn.datnbe.student.dto.response.StudentResponseDto;
import com.datn.datnbe.student.entity.Student;
import com.datn.datnbe.student.mapper.StudentEntityMapper;
import com.datn.datnbe.student.repository.StudentRepository;
import com.datn.datnbe.sharedkernel.exceptions.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Management service for student CRUD operations.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class StudentManagement implements StudentApi {

    StudentRepository studentRepository;
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

        studentRepository.deleteById(id);
        log.info("Successfully deleted student with ID: {}", id);
    }
}
