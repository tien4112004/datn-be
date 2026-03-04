package com.datn.datnbe.payment.service;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.datn.datnbe.payment.dto.CoinPackageDto;
import com.datn.datnbe.payment.dto.request.CreateCoinPackageRequest;
import com.datn.datnbe.payment.dto.request.UpdateCoinPackageRequest;
import com.datn.datnbe.payment.entity.CoinPackage;
import com.datn.datnbe.payment.mapper.PaymentMapper;
import com.datn.datnbe.payment.repository.CoinPackageRepository;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoinPackageService {

    private final CoinPackageRepository coinPackageRepository;
    private final PaymentMapper mapper;

    /**
     * Get all active coin packages sorted by sort order
     */
    @Transactional(readOnly = true)
    public List<CoinPackageDto> getAllActivePackages() {
        List<CoinPackage> packages = coinPackageRepository.findAllActive();
        return packages.stream().map(mapper::toCoinPackageDTO).collect(Collectors.toList());
    }

    /**
     * Get all coin packages (including inactive)
     */
    @Transactional(readOnly = true)
    public List<CoinPackageDto> getAllPackages() {
        List<CoinPackage> packages = coinPackageRepository.findAll();
        return packages.stream().map(mapper::toCoinPackageDTO).collect(Collectors.toList());
    }

    /**
     * Get a specific coin package by ID
     */
    @Transactional(readOnly = true)
    public CoinPackageDto getPackageById(String packageId) {
        CoinPackage coinPackage = coinPackageRepository.findById(packageId).orElseThrow(() -> {
            log.warn("Coin package not found with ID: {}", packageId);
            return new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Coin package not found");
        });

        return mapper.toCoinPackageDTO(coinPackage);
    }

    /**
     * Get coin package entity by ID (internal use)
     */
    @Transactional(readOnly = true)
    public CoinPackage getPackageEntityById(String packageId) {
        return coinPackageRepository.findById(packageId).orElseThrow(() -> {
            log.warn("Coin package not found with ID: {}", packageId);
            return new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Coin package not found");
        });
    }

    /**
     * Create a new coin package
     */
    @Transactional
    public CoinPackageDto createCoinPackage(CreateCoinPackageRequest request) {
        log.info("Creating new coin package: {}", request.getName());

        // Check if package with same name already exists
        if (coinPackageRepository.existsByName(request.getName())) {
            log.warn("Coin package with name already exists: {}", request.getName());
            throw new AppException(ErrorCode.RESOURCE_ALREADY_EXISTS, "Coin package with this name already exists");
        }

        CoinPackage coinPackage = CoinPackage.builder()
                .id(UUID.randomUUID().toString())
                .name(request.getName())
                .price(request.getPrice())
                .coin(request.getCoin())
                .bonus(request.getBonus())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();

        CoinPackage savedPackage = coinPackageRepository.save(coinPackage);
        log.info("Coin package created successfully with ID: {}", savedPackage.getId());

        return mapper.toCoinPackageDTO(savedPackage);
    }

    /**
     * Update an existing coin package
     */
    @Transactional
    public CoinPackageDto updateCoinPackage(String packageId, UpdateCoinPackageRequest request) {
        log.info("Updating coin package: {}", packageId);

        CoinPackage coinPackage = getPackageEntityById(packageId);

        // Check if another package with the same name already exists
        if (!coinPackage.getName().equals(request.getName()) && coinPackageRepository.existsByName(request.getName())) {
            log.warn("Coin package with name already exists: {}", request.getName());
            throw new AppException(ErrorCode.RESOURCE_ALREADY_EXISTS, "Coin package with this name already exists");
        }

        coinPackage.setName(request.getName());
        coinPackage.setPrice(request.getPrice());
        coinPackage.setCoin(request.getCoin());
        coinPackage.setBonus(request.getBonus());
        if (request.getIsActive() != null) {
            coinPackage.setIsActive(request.getIsActive());
        }
        coinPackage.setUpdatedAt(new Date());

        CoinPackage updatedPackage = coinPackageRepository.save(coinPackage);
        log.info("Coin package updated successfully: {}", packageId);

        return mapper.toCoinPackageDTO(updatedPackage);
    }

    /**
     * Delete a coin package
     */
    @Transactional
    public void deleteCoinPackage(String packageId) {
        log.info("Deleting coin package: {}", packageId);

        CoinPackage coinPackage = getPackageEntityById(packageId);
        coinPackageRepository.delete(coinPackage);

        log.info("Coin package deleted successfully: {}", packageId);
    }

    /**
     * Toggle coin package active status
     */
    @Transactional
    public CoinPackageDto togglePackageStatus(String packageId) {
        log.info("Toggling status for coin package: {}", packageId);

        CoinPackage coinPackage = getPackageEntityById(packageId);
        coinPackage.setIsActive(!coinPackage.getIsActive());
        coinPackage.setUpdatedAt(new Date());

        CoinPackage updatedPackage = coinPackageRepository.save(coinPackage);
        log.info("Coin package status toggled: {} (isActive: {})", packageId, updatedPackage.getIsActive());

        return mapper.toCoinPackageDTO(updatedPackage);
    }
}
