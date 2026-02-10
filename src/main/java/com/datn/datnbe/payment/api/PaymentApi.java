package com.datn.datnbe.payment.api;

import com.datn.datnbe.payment.dto.CoinUsageTransactionDTO;
import com.datn.datnbe.payment.dto.UserCoinDTO;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import org.springframework.data.domain.Pageable;

public interface PaymentApi {

    /**
     * Get current coin balance of a user.
     *
     * @param userId the user ID
     * @return user coin data
     */
    UserCoinDTO getUserCoin(String userId);

    /**
     * Initialize coins for a newly registered user.
     *
     * @param userId the user ID
     * @return initialized user coin data
     */
    UserCoinDTO initializeUserCoin(String userId);

    /**
     * Subtract coins from a user's balance.
     *
     * @param userId the user ID
     * @param amount amount of coins to subtract
     * @param source the source/reason for the transaction
     * @return updated user coin data
     */
    UserCoinDTO subtractCoin(String userId, Long amount, String source);

    /**
     * Get coin usage transaction history with pagination and filters.
     *
     * @param userId the user ID
     * @param type filter by transaction type (optional, "ADD" or "SUBTRACT")
     * @param source filter by transaction source (optional)
     * @param pageable pagination parameters
     * @return paginated list of coin transactions
     */
    PaginatedResponseDto<CoinUsageTransactionDTO> getCoinHistory(String userId,
            String type,
            String source,
            Pageable pageable);
}
