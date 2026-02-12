package com.datn.datnbe.student.api;

import com.datn.datnbe.student.repository.ClassEnrollmentRepository;
import com.datn.datnbe.student.repository.StudentRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ClassApi {
    ClassEnrollmentRepository classEnrollmentRepository;
    StudentRepository studentRepository;

}
