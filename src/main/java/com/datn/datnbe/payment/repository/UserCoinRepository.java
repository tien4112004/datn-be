package com.datn.datnbe.payment.repository;

import com.datn.datnbe.payment.entity.UserCoin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCoinRepository extends JpaRepository<UserCoin, String> {
}
