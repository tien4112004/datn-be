package com.datn.document.repository.interfaces;

import com.datn.document.entity.Presentation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PresentationRepository extends MongoRepository<Presentation, String> {
}
