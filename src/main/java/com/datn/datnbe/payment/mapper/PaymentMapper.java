package com.datn.datnbe.payment.mapper;

import com.datn.datnbe.payment.dto.CoinUsageTransactionDTO;
import com.datn.datnbe.payment.dto.UserCoinDTO;
import com.datn.datnbe.payment.entity.CoinUsageTransaction;
import com.datn.datnbe.payment.entity.UserCoin;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    UserCoinDTO toUserCoinDTO(UserCoin userCoin);

    UserCoin toUserCoinEntity(UserCoinDTO userCoinDTO);

    CoinUsageTransactionDTO toCoinUsageTransactionDTO(CoinUsageTransaction transaction);

    CoinUsageTransaction toCoinUsageTransactionEntity(CoinUsageTransactionDTO dto);
}
