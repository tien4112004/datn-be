package com.datn.aiservice.repository.impl;

import com.datn.aiservice.repository.impl.jpa.SpringDataJPAModelConfigurationRepo;
import com.datn.aiservice.repository.interfaces.ModelConfigurationRepo;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ModelConfigurationImpl implements ModelConfigurationRepo {

    SpringDataJPAModelConfigurationRepo springDataJPAModelConfigurationRepo;

    // Example method implementation
    @Override
    public String getModelName() {
        return "Default Model Name"; // Replace with actual logic
    }

    @Override
    public boolean isModelEnabled(String modelName) {
        // Here you would typically check the model configuration in the database
        // For demonstration purposes, let's assume all models are enabled
        return true;
    }
}
