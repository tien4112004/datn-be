package com.datn.datnbe.student.repository;

import com.datn.datnbe.student.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface StudentRepository extends JpaRepository<Student, String> {

    List<Student> findByIdIn(Set<String> ids);

    Page<Student> findByIdIn(Set<String> ids, Pageable pageable);

    boolean existsByIdIn(Set<String> ids);

    Optional<Student> findByUserId(String userId);

    boolean existsByUserId(String userId);

    @Query("SELECT COUNT(up) FROM Student s JOIN UserProfile up ON s.userId = up.id " + "WHERE up.email LIKE :pattern")
    int countExistingUsernames(@Param("pattern") String pattern);
}
