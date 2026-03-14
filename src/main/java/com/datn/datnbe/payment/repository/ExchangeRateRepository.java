package com.datn.datnbe.payment.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.datn.datnbe.payment.entity.ExchangeRate;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, String> {
    
    @Query("SELECT e FROM ExchangeRate e WHERE e.date = :date")
    ExchangeRate findByDate(@Param("date") LocalDate date);

    ExchangeRate findTopByDateLessThanEqualOrderByDateDesc(LocalDate date);

    ExchangeRate findTopByOrderByDateDesc();
}
