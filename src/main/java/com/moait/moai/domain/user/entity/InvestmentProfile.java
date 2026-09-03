package com.moait.moai.domain.user.entity;

import com.moait.moai.common.entity.BaseCreatedEntity;
import com.moait.moai.common.enums.InvestmentExperience;
import com.moait.moai.common.enums.InvestmentHorizon;
import com.moait.moai.common.enums.LossReaction;
import com.moait.moai.common.enums.RiskProfileType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 개인 투자성향 설문 결과. 재설문 시 새 row 를 쌓고 {@code is_latest} 로 최신을 구분한다.
 */
@Entity
@Table(name = "investment_profile")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InvestmentProfile extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "investment_experience", length = 20)
    private InvestmentExperience investmentExperience;

    @Enumerated(EnumType.STRING)
    @Column(name = "loss_reaction", length = 20)
    private LossReaction lossReaction;

    @Column(name = "max_tolerable_loss_rate")
    private Integer maxTolerableLossRate;

    @Enumerated(EnumType.STRING)
    @Column(name = "investment_horizon", length = 20)
    private InvestmentHorizon investmentHorizon;

    @Convert(converter = HoldingAssetsConverter.class)
    @Column(name = "holding_assets")
    private List<HoldingAsset> holdingAssets;

    @Column(name = "monthly_investable_amount")
    private Long monthlyInvestableAmount;

    @Column(name = "emergency_fund_secured")
    private Boolean emergencyFundSecured;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_profile_type", length = 20)
    private RiskProfileType riskProfileType;

    @Column(name = "risk_profile_score")
    private Integer riskProfileScore;

    @Column(name = "is_latest")
    private Boolean isLatest;

    @SuppressWarnings("java:S107") // 설문 필드가 많아 파라미터 수가 큼 — 팩토리에서만 사용
    private InvestmentProfile(Long userId, InvestmentExperience investmentExperience,
                             LossReaction lossReaction, Integer maxTolerableLossRate,
                             InvestmentHorizon investmentHorizon, List<HoldingAsset> holdingAssets,
                             Long monthlyInvestableAmount, Boolean emergencyFundSecured,
                             RiskProfileType riskProfileType, Integer riskProfileScore) {
        this.userId = userId;
        this.investmentExperience = investmentExperience;
        this.lossReaction = lossReaction;
        this.maxTolerableLossRate = maxTolerableLossRate;
        this.investmentHorizon = investmentHorizon;
        this.holdingAssets = holdingAssets;
        this.monthlyInvestableAmount = monthlyInvestableAmount;
        this.emergencyFundSecured = emergencyFundSecured;
        this.riskProfileType = riskProfileType;
        this.riskProfileScore = riskProfileScore;
        this.isLatest = true;
    }

    public static InvestmentProfile create(Long userId, InvestmentExperience investmentExperience,
                                           LossReaction lossReaction, Integer maxTolerableLossRate,
                                           InvestmentHorizon investmentHorizon,
                                           List<HoldingAsset> holdingAssets,
                                           Long monthlyInvestableAmount, Boolean emergencyFundSecured,
                                           RiskProfileType riskProfileType, Integer riskProfileScore) {
        return new InvestmentProfile(userId, investmentExperience, lossReaction, maxTolerableLossRate,
                investmentHorizon, holdingAssets, monthlyInvestableAmount, emergencyFundSecured,
                riskProfileType, riskProfileScore);
    }

    /** 재설문 시 이전 프로필을 최신 아님으로 표시. */
    public void markStale() {
        this.isLatest = false;
    }
}
