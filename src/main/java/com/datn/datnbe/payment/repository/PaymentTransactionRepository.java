package com.datn.datnbe.payment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.datn.datnbe.payment.entity.PaymentTransaction;
import com.datn.datnbe.payment.entity.PaymentTransaction.TransactionStatus;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, String> {

    Optional<PaymentTransaction> findBySepayTransactionId(String sepayTransactionId);

    Optional<PaymentTransaction> findByReferenceCode(String referenceCode);

    Optional<PaymentTransaction> findByOrderInvoiceNumber(String orderInvoiceNumber);

    Page<PaymentTransaction> findByUserId(String userId, Pageable pageable);

    List<PaymentTransaction> findByUserIdAndStatus(String userId, TransactionStatus status);

    @Query("SELECT p FROM PaymentTransaction p WHERE p.status = :status")
    List<PaymentTransaction> findByStatus(@Param("status") TransactionStatus status);
}
