package com.datn.datnbe.payment.repository;

import com.datn.datnbe.payment.entity.CoinUsageTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoinUsageTransactionRepository extends JpaRepository<CoinUsageTransaction, String> {
    List<CoinUsageTransaction> findByUserIdOrderByCreatedAtDesc(String userId);
}
