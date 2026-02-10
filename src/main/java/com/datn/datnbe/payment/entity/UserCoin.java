package com.datn.datnbe.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_coins")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCoin {

    @Id
    private String id; // user_id

    @Column(name = "coin", nullable = false)
    private Long coin;

    public UserCoin(String userId) {
        this.id = userId;
        this.coin = 0L;
    }
}
