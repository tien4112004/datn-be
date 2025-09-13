package com.datn.datnbe.ai.presentation;

import com.datn.datnbe.ai.api.ModelSelectionApi;
import com.datn.datnbe.ai.dto.request.UpdateModelStatusRequest;
import com.datn.datnbe.ai.dto.response.ModelResponseDto;
import com.datn.datnbe.ai.enums.ModelType;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import java.util.List;

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
    ModelSelectionApi modelSelectionApi;

    /**
     * Endpoint to get the list of all models.
     *
     * @return ResponseEntity containing the list of models.
     */
    @GetMapping
    public ResponseEntity<AppResponseDto<List<ModelResponseDto>>> getAllModels(
            @RequestParam(name = "modelType", required = false) ModelType modelType) {
        log.info("Fetching all models");
        var models = modelSelectionApi.getModelConfigurations(modelType);

        return ResponseEntity.ok(AppResponseDto.success(models));
    }

    /**
     * Endpoint to update status of a model by its ID.
     *
     * @param id      The ID of the model to update.
     * @param request The update request containing isEnabled and/or isDefault
     *                status.
     * @return ResponseEntity indicating the result of the operation.
     */
    @PatchMapping(value = "/{id}")
    public ResponseEntity<AppResponseDto<ModelResponseDto>> updateModelStatus(@PathVariable Integer id,
            @RequestBody UpdateModelStatusRequest request) {
        log.info("Updating model status with ID: {}", id);
        var updatedModel = modelSelectionApi.setModelStatus(id, request);

        return ResponseEntity.ok(AppResponseDto.success(updatedModel));
    }
}
