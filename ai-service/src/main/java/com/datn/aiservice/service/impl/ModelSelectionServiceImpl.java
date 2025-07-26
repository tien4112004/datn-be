package com.datn.aiservice.service.impl;

import com.datn.aiservice.config.ChatModelConfiguration.ModelProperties.ModelInfo;
import com.datn.aiservice.dto.response.ModelMinimalResponseDto;
import com.datn.aiservice.dto.response.ModelResponseDto;
import com.datn.aiservice.factory.ChatClientFactory;
import com.datn.aiservice.repository.interfaces.ModelConfigurationRepo;
import com.datn.aiservice.service.interfaces.ModelSelectionService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class ModelSelectionServiceImpl implements ModelSelectionService {
    ModelConfigurationRepo modelConfigurationRepo;

    @Override
    public List<ModelMinimalResponseDto> getModelsConfiguration() {
        throw new UnsupportedOperationException("Get model configurations is not supported");
    }

    @Override
    public ModelResponseDto getFullModelConfiguration(String modelId) {
        throw new UnsupportedOperationException("Get model configurations is not supported");
    }

    @Override
    public void setModelEnabled(String modelId, boolean isEnabled) {
        throw new UnsupportedOperationException("Update model state is not supported");
    }

    @Override
    public void setDefaultModel(String modelId) {
        throw new UnsupportedOperationException("Set default model is not supported");
    }

    @Override
    public boolean isModelEnabled(String modelName) {
        return false;
    }

    @Override
    public void saveModelData(ModelInfo modelInfo) {
        // TODO Auto-generated method stub
        // From modelInfo:
        // 1. create moedlentity
        // 2. save model entity
        throw new UnsupportedOperationException("Unimplemented method 'saveModelData'");
    }

}
