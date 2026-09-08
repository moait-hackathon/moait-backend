package com.moait.moai.domain.home.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "investment_asset")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HomeInvestmentAsset {

    @Id
    @Column(name = "investment_asset_id")
    private Long id;

    @Column(name = "investment_account_id", nullable = false)
    private Long investmentAccountId;

    @Column(name = "principal_amount", nullable = false)
    private BigDecimal principalAmount;

    @Column(name = "current_value", nullable = false)
    private BigDecimal currentValue;

    @Column(name = "evaluation_profit_loss", nullable = false)
    private BigDecimal evaluationProfitLoss;

    @Column(name = "is_active", nullable = false)
    private Boolean active;
}
