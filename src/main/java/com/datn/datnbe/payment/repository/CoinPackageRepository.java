package com.datn.datnbe.payment.repository;

import com.datn.datnbe.payment.entity.CoinPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CoinPackageRepository extends JpaRepository<CoinPackage, String> {

    /**
     * Get all active coin packages
     */
    @Query("SELECT cp FROM CoinPackage cp WHERE cp.isActive = true ORDER BY cp.price ASC")
    List<CoinPackage> findAllActive();

    /**
     * Get a coin package by name
     */
    Optional<CoinPackage> findByName(String name);

    /**
     * Get a coin package by price (to find bonus coins after payment)
     */
    Optional<CoinPackage> findByPrice(Long price);
}
