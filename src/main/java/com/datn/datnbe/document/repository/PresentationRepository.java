package com.datn.datnbe.document.repository;

import com.datn.datnbe.document.entity.Presentation;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PresentationRepository extends MongoRepository<Presentation, String> {

    @Query("{ 'title': { $regex: ?0, $options: 'i' }, 'deleted_at': null }")
    Page<Presentation> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    @Query("{ 'deleted_at': null }")
    Page<Presentation> findAll(Pageable pageable);

    @Query("{ 'title': ?0, 'deleted_at': null }")
    boolean existsByTitle(String title);

    @Query("{ '_id': ?0, 'deleted_at': null }")
    Optional<Presentation> findById(ObjectId id);
}
