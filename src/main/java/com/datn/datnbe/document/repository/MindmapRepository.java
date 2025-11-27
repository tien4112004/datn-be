package com.datn.datnbe.document.repository;

import com.datn.datnbe.document.entity.Mindmap;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MindmapRepository extends MongoRepository<Mindmap, String> {
    @Query("{ 'title': { $regex: ?0, $options: 'i' } }")
    Page<Mindmap> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Mindmap> findAll(Pageable pageable);

    boolean existsByTitle(String title);

    @Query("{ '_id': { $in: ?0 }, 'deleted_at': null }")
    Page<Mindmap> findByIdIn(Iterable<String> ids, Pageable pageable);
}
