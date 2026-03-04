package com.datn.datnbe.payment.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.datn.datnbe.payment.dto.CoinPackageDto;
import com.datn.datnbe.payment.dto.request.CreateCoinPackageRequest;
import com.datn.datnbe.payment.dto.request.UpdateCoinPackageRequest;
import com.datn.datnbe.payment.service.CoinPackageService;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@RestController
@RequestMapping("/api/coin-packages")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
        RequestMethod.DELETE})
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CoinPackageController {

    CoinPackageService coinPackageService;

    /**
     * Get all available coin packages for user
     * Endpoint: GET /api/coin-packages
     */
    @GetMapping
    public ResponseEntity<AppResponseDto<List<CoinPackageDto>>> getAllCoinPackages() {
        log.info("Fetching all available coin packages");

        List<CoinPackageDto> packages = coinPackageService.getAllPackages();

        return ResponseEntity.ok(AppResponseDto.<List<CoinPackageDto>>builder()
                .data(packages)
                .build());
    }

    /**
     * Get a specific coin package by ID
     * Endpoint: GET /api/coin-packages/{packageId}
     */
    @GetMapping("/{packageId}")
    public ResponseEntity<AppResponseDto<CoinPackageDto>> getCoinPackage(@PathVariable String packageId) {
        log.info("Fetching coin package: {}", packageId);

        CoinPackageDto packageDto = coinPackageService.getPackageById(packageId);

        return ResponseEntity.ok(AppResponseDto.<CoinPackageDto>builder()
                .data(packageDto)
                .build());
    }

    /**
     * Create a new coin package
     * Endpoint: POST /api/coin-packages
     */
    @PostMapping
    public ResponseEntity<AppResponseDto<CoinPackageDto>> createCoinPackage(
            @Valid @RequestBody CreateCoinPackageRequest request) {
        log.info("Creating new coin package: {}", request.getName());

        CoinPackageDto packageDto = coinPackageService.createCoinPackage(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AppResponseDto.<CoinPackageDto>builder()
                        .data(packageDto)
                        .build());
    }

    /**
     * Update a coin package
     * Endpoint: PUT /api/coin-packages/{packageId}
     */
    @PutMapping("/{packageId}")
    public ResponseEntity<AppResponseDto<CoinPackageDto>> updateCoinPackage(@PathVariable String packageId,
            @Valid @RequestBody UpdateCoinPackageRequest request) {
        log.info("Updating coin package: {}", packageId);

        CoinPackageDto packageDto = coinPackageService.updateCoinPackage(packageId, request);

        return ResponseEntity.ok(AppResponseDto.<CoinPackageDto>builder()
                .data(packageDto)
                .build());
    }

    /**
     * Delete a coin package
     * Endpoint: DELETE /api/coin-packages/{packageId}
     */
    @DeleteMapping("/{packageId}")
    public ResponseEntity<AppResponseDto<Void>> deleteCoinPackage(@PathVariable String packageId) {
        log.info("Deleting coin package: {}", packageId);

        coinPackageService.deleteCoinPackage(packageId);

        return ResponseEntity.noContent().build();
    }

    /**
     * Toggle coin package active status
     * Endpoint: PATCH /api/coin-packages/{packageId}/toggle-status
     */
    @PatchMapping("/{packageId}/toggle-status")
    public ResponseEntity<AppResponseDto<CoinPackageDto>> togglePackageStatus(@PathVariable String packageId) {
        log.info("Toggling status for coin package: {}", packageId);

        CoinPackageDto packageDto = coinPackageService.togglePackageStatus(packageId);

        return ResponseEntity.ok(AppResponseDto.<CoinPackageDto>builder()
                .data(packageDto)
                .build());
    }
}
