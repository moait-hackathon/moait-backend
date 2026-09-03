package com.moait.moai.domain.user.entity;

import com.moait.moai.common.entity.BaseCreatedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 개인 투자성향 설문 결과. 재설문 시 새 row 를 쌓고 {@code is_latest} 로 최신을 구분한다.
 *
 * <p>이번 슬라이스(인증)에서는 온보딩 단계 판정을 위한 조회에만 사용한다.
 * 설문 제출/enum 매핑/{@code holding_assets}(JSON) 매핑은 user 도메인 슬라이스에서 채운다.
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

    @Column(name = "investment_experience", length = 20)
    private String investmentExperience;

    @Column(name = "loss_reaction", length = 20)
    private String lossReaction;

    @Column(name = "max_tolerable_loss_rate")
    private Integer maxTolerableLossRate;

    @Column(name = "investment_horizon", length = 20)
    private String investmentHorizon;

    // TODO(user 슬라이스): holding_assets(JSON) 매핑

    @Column(name = "monthly_investable_amount")
    private Long monthlyInvestableAmount;

    @Column(name = "emergency_fund_secured")
    private Boolean emergencyFundSecured;

    @Column(name = "risk_profile_type", length = 20)
    private String riskProfileType;

    @Column(name = "risk_profile_score")
    private Integer riskProfileScore;

    @Column(name = "is_latest")
    private Boolean isLatest;
}
