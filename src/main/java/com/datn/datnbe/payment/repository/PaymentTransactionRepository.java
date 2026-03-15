package com.datn.datnbe.payment.repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.datn.datnbe.payment.entity.PaymentTransaction;
import com.datn.datnbe.payment.entity.PaymentTransaction.TransactionStatus;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, String> {

    Optional<PaymentTransaction> findBySepayTransactionId(String sepayTransactionId);

    Optional<PaymentTransaction> findByReferenceCode(String referenceCode);

    Optional<PaymentTransaction> findByOrderInvoiceNumber(String orderInvoiceNumber);

    Page<PaymentTransaction> findByUserId(String userId, Pageable pageable);

    List<PaymentTransaction> findByUserIdAndStatus(String userId, TransactionStatus status);

    List<PaymentTransaction> findByStatus(TransactionStatus status);

    @Query("SELECT p FROM PaymentTransaction p WHERE (:status IS NULL OR p.status = :status) ORDER BY p.createdAt DESC")
    Page<PaymentTransaction> findAllWithFilter(@Param("status") TransactionStatus status, Pageable pageable);

    @Query(value = "SELECT COALESCE(SUM(amount), 0) FROM payment_transactions WHERE status = 'COMPLETED'", nativeQuery = true)
    BigDecimal getTotalRevenue();

    @Query(value = "SELECT COUNT(*) FROM payment_transactions WHERE status = 'COMPLETED'", nativeQuery = true)
    Long getTotalCompletedTransactions();

    @Query(value = """
            SELECT DATE(completed_at) AS transaction_date, COALESCE(SUM(amount), 0) AS total_amount
            FROM payment_transactions
            WHERE status = 'COMPLETED'
              AND completed_at >= :fromDate
              AND completed_at < :toDateExclusive
            GROUP BY DATE(completed_at)
            ORDER BY transaction_date
            """, nativeQuery = true)
    List<Object[]> sumCompletedAmountByDateRange(@Param("fromDate") Date fromDate,
            @Param("toDateExclusive") Date toDateExclusive);

    @Query(value = """
            SELECT TO_CHAR(completed_at, 'YYYY-MM') AS month, COALESCE(SUM(amount), 0) AS revenue
            FROM payment_transactions
            WHERE status = 'COMPLETED' AND completed_at >= :since
            GROUP BY TO_CHAR(completed_at, 'YYYY-MM')
            ORDER BY month
            """, nativeQuery = true)
    List<Object[]> sumRevenueByMonth(@Param("since") Date since);

    @Query(value = """
            SELECT DATE(created_at) AS transaction_date, COALESCE(SUM(actual_price), 0) AS total_amount
            FROM token_usage
            WHERE created_at >= :fromDate
              AND created_at < :toDateExclusive
              AND actual_price IS NOT NULL
            GROUP BY DATE(created_at)
            ORDER BY transaction_date
            """, nativeQuery = true)
    List<Object[]> sumCostByDateRange(@Param("fromDate") Date fromDate, @Param("toDateExclusive") Date toDateExclusive);
}
