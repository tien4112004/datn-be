package com.datn.datnbe.document.repository;

import com.datn.datnbe.document.entity.Presentation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PresentationRepository extends MongoRepository<Presentation, String> {

    @Query("{ 'title': { $regex: ?0, $options: 'i' } }")
    Page<Presentation> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Presentation> findAll(Pageable pageable);

    boolean existsByTitle(String title);
}
