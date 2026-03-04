package com.datn.datnbe.ai.repository;

import com.datn.datnbe.ai.entity.TeacherSystemPrompt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TeacherSystemPromptRepository extends JpaRepository<TeacherSystemPrompt, UUID> {

    Optional<TeacherSystemPrompt> findByTeacherId(String teacherId);

    void deleteByTeacherId(String teacherId);
}
