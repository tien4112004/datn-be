package com.datn.aiservice.controller;

import com.datn.aiservice.dto.response.common.AppResponseDto;
import com.datn.aiservice.service.interfaces.ModelSelectionService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/models")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class ModelConfigurationController {
     ModelSelectionService modelSelectionService;

    /**
     * Endpoint to get the list of all models.
     *
     * @return ResponseEntity containing the list of models.
     */
    @GetMapping
    public ResponseEntity<AppResponseDto<?>> getAllModels() {
        log.info("Fetching all models");
        // Assuming a service method exists to fetch all models
        var models = modelSelectionService.getModelConfigurations();
        return ResponseEntity.ok(AppResponseDto.success(models, "Fetched all models successfully"));
    }

    /**
     * Endpoint to enable or disable a model by its ID.
     *
     * @param id        The ID of the model to enable/disable.
     * @return ResponseEntity indicating the result of the operation.
     */
    @GetMapping(value = "/{id}")
    public ResponseEntity<AppResponseDto<?>> getModelById(@PathVariable String id) {
        log.info("Fetching model with ID: {}", id);
        // Assuming a service method exists to fetch the model by ID
        var model = modelSelectionService.getModelConfiguration(id);
        return ResponseEntity.ok(AppResponseDto.success(model, "Fetched model successfully"));
    }

    /**
     * Endpoint to disable a model by its ID.
     *
     * @param id The ID of the model to disable.
     * @return ResponseEntity indicating the result of the operation.
     */
    @PostMapping(value = "/{id}")
    public ResponseEntity<AppResponseDto<Void>> updateModelStatus(
            @PathVariable String id,
            @RequestParam(name = "enable", required = false, defaultValue = "true") boolean isEnable) {
        // Logic to enable the model
        log.info("Enabling model with ID: {}", id);
        // Assuming a service method exists to handle this
        modelSelectionService.setModelEnabled(id, isEnable);
        return ResponseEntity.ok(AppResponseDto.success());
    }
}
