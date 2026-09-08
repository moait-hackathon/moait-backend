package com.moait.moai.domain.home.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "investment_account")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HomeInvestmentAccount {

    @Id
    @Column(name = "investment_account_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "is_active", nullable = false)
    private Boolean active;
}
