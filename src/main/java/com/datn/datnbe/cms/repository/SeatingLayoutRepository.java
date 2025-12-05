package com.datn.datnbe.cms.repository;

import com.datn.datnbe.cms.entity.SeatingLayout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SeatingLayoutRepository extends JpaRepository<SeatingLayout, String> {

    @Query("SELECT s FROM SeatingLayout s WHERE s.classEntity.id = :classId")
    Optional<SeatingLayout> findByClassId(@Param("classId") String classId);
}
