package com.datn.datnbe.payment.mapper;

import com.datn.datnbe.payment.dto.CoinUsageTransactionDto;
import com.datn.datnbe.payment.dto.UserCoinDto;
import com.datn.datnbe.payment.entity.CoinUsageTransaction;
import com.datn.datnbe.payment.entity.UserCoin;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    UserCoinDto toUserCoinDTO(UserCoin userCoin);

    UserCoin toUserCoinEntity(UserCoinDto userCoinDTO);

    CoinUsageTransactionDto toCoinUsageTransactionDTO(CoinUsageTransaction transaction);

    CoinUsageTransaction toCoinUsageTransactionEntity(CoinUsageTransactionDto dto);
}
