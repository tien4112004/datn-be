package com.datn.datnbe.student.repository;

import com.datn.datnbe.student.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface StudentRepository extends JpaRepository<Student, String> {

    List<Student> findByIdIn(Set<String> ids);

    boolean existsByIdIn(Set<String> ids);

    Optional<Student> findByEmail(String email);

    boolean existsByEmail(String email);
}
