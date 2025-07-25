package com.datn.aiservice.repository.impl.jpa;

import com.datn.aiservice.repository.interfaces.ModelConfigurationRepo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataJPAModelConfigurationRepo extends MongoRepository<ModelConfigurationRepo, String> {
}
