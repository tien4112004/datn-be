package com.datn.datnbe.payment.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.datn.datnbe.payment.dto.CoinPackageDto;
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
     * Get a specific coin package by ID
     */
    @Transactional(readOnly = true)
    public CoinPackageDto getPackageById(String packageId) {
        CoinPackage coinPackage = coinPackageRepository.findById(packageId).orElseThrow(() -> {
            log.warn("Coin package not found with ID: {}", packageId);
            return new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Coin package not found");
        });

        if (!coinPackage.getIsActive()) {
            log.warn("Coin package is not active: {}", packageId);
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Coin package is not available");
        }

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
}
