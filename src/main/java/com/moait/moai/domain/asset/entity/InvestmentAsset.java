package com.moait.moai.domain.asset.entity;

import com.moait.moai.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 투자성향 분석에 사용하는 보유 자산의 읽기 매핑. */
@Entity
@Table(name = "investment_asset")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InvestmentAsset extends BaseTimeEntity {
    @Id
    @Column(name = "investment_asset_id")
    private Long id;

    @Column(name = "investment_account_id", nullable = false)
    private Long investmentAccountId;

    @Column(name = "asset_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private AssetType assetType;

    @Column(name = "risk_level", nullable = false)
    @Enumerated(EnumType.STRING)
    private RiskLevel riskLevel;

    @Column(name = "current_value", nullable = false, precision = 19, scale = 2)
    private BigDecimal currentValue;

    @Column(name = "currency_code", nullable = false, length = 3, columnDefinition = "char(3)")
    private String currencyCode;

    @Column(name = "is_active", nullable = false)
    private Boolean active;

    public enum AssetType { STOCK, ETF, FUND, BOND, DEPOSIT, SAVINGS, PENSION, CASH, CRYPTO, OTHER }
    public enum RiskLevel { VERY_LOW, LOW, MEDIUM, HIGH, VERY_HIGH, UNCLASSIFIED }
}
