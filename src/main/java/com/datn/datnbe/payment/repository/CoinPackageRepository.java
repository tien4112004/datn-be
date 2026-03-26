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
    @Query("SELECT cp FROM CoinPackage cp WHERE cp.isActive = true ORDER BY cp.coin ASC")
    List<CoinPackage> findAllActive();

    /**
     * Get all coin packages sorted by coin amount
     */
    @Query("SELECT cp FROM CoinPackage cp ORDER BY cp.coin ASC")
    List<CoinPackage> findAllOrderByCoinAsc();

    /**
     * Get a coin package by name
     */
    Optional<CoinPackage> findByName(String name);

    /**
     * Get an active coin package by price (to find bonus coins after payment)
     * If multiple packages have the same price, returns the one with highest bonus
     */
    @Query("SELECT cp FROM CoinPackage cp WHERE cp.price = :price AND cp.isActive = true ORDER BY cp.bonus DESC LIMIT 1")
    Optional<CoinPackage> findByPrice(Long price);

    /**
     * Check if a coin package with given name exists
     */
    boolean existsByName(String name);
}
