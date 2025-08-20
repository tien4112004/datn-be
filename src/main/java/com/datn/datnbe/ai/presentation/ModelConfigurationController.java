package com.datn.datnbe.ai.presentation;

import com.datn.datnbe.ai.api.ModelSelectionApi;
import com.datn.datnbe.ai.dto.response.ModelResponseDto;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/models")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class ModelConfigurationController {
    ModelSelectionApi modelSelectionApi;

    /**
     * Endpoint to get the list of all models.
     *
     * @return ResponseEntity containing the list of models.
     */
    @GetMapping
    public ResponseEntity<AppResponseDto<List<ModelResponseDto>>> getAllModels() {
        log.info("Fetching all models");
        var models = modelSelectionApi.getModelConfigurations();
        return ResponseEntity.ok(AppResponseDto.success(models, "Fetched all models successfully"));
    }

    /**
     * Endpoint to enable or disable a model by its ID.
     *
     * @param id The ID of the model to enable/disable.
     * @return ResponseEntity indicating the result of the operation.
     */
    @GetMapping(value = "/{id}")
    public ResponseEntity<AppResponseDto<ModelResponseDto>> getModelById(@PathVariable Integer id) {
        log.info("Fetching model with ID: {}", id);
        var model = modelSelectionApi.getModelConfiguration(id);
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
            @PathVariable Integer id,
            @RequestParam(name = "enable", required = false)  Boolean isEnable,
            @RequestParam(name = "default", required = false) Boolean isDefault) {
        log.info("Enabling model with ID: {}", id);

        modelSelectionApi.setModelStatus(id, isEnable, isDefault);

        return ResponseEntity.ok(AppResponseDto.success());
    }
}
