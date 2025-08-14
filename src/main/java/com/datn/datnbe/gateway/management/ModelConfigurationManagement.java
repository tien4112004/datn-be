package com.datn.datnbe.gateway.management;

import com.datn.datnbe.ai.api.ModelSelectionApi;
import com.datn.datnbe.ai.dto.response.common.AppResponseDto;
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
public class ModelConfigurationManagement {
    ModelSelectionApi modelSelectionApi;

    /**
     * Endpoint to get the list of all models.
     *
     * @return ResponseEntity containing the list of models.
     */
    @GetMapping
    public ResponseEntity<AppResponseDto<?>> getAllModels() {
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
    public ResponseEntity<AppResponseDto<?>> getModelById(@PathVariable Integer id) {
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
    public ResponseEntity<AppResponseDto<Void>> updateModelStatus(@PathVariable Integer id,
                                                                  @RequestParam(name = "enable", required = false, defaultValue = "true") boolean isEnable,
                                                                  @RequestParam(name = "default", required = false, defaultValue = "false") boolean isDefault) {
        log.info("Enabling model with ID: {}", id);

        modelSelectionApi.setModelEnabled(id, isEnable);
        modelSelectionApi.setModelDefault(id, isDefault);

        return ResponseEntity.ok(AppResponseDto.success());
    }
}
