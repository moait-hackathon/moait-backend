package com.moait.moai.domain.home.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "investment_asset_snapshot")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HomeInvestmentAssetSnapshot {

    @Id
    @Column(name = "snapshot_id")
    private Long id;

    @Column(name = "investment_asset_id", nullable = false)
    private Long investmentAssetId;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "principal_amount", nullable = false)
    private BigDecimal principalAmount;

    @Column(name = "current_value", nullable = false)
    private BigDecimal currentValue;

    @Column(name = "evaluation_profit_loss", nullable = false)
    private BigDecimal evaluationProfitLoss;
}
