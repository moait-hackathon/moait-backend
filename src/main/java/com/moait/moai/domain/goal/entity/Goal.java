package com.moait.moai.domain.goal.entity;

import com.moait.moai.common.entity.BaseTimeEntity;
import com.moait.moai.common.enums.EmergencyFundMonths;
import com.moait.moai.common.enums.GoalStatus;
import com.moait.moai.common.enums.InvestmentExperience;
import com.moait.moai.common.enums.LossReaction;
import com.moait.moai.common.enums.MonthlySurplusBand;
import com.moait.moai.common.enums.RiskProfileType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 커플 공동 목표 + 공동 투자성향. 커플당 1개 ({@code UNIQUE(couple_id)}), 덮어쓰기 / 이력 없음.
 *
 * <p>{@code POST /goals} 온보딩 5단계 제출로 생성({@code ACTIVE}). 마이페이지에서 각 값 수정 가능하며
 * 점수 문항·{@code targetDate} 변경 시 {@code investmentPeriodMonths}/{@code riskProfileScore}/
 * {@code jointRiskProfileType} 를 재계산한다.
 */
@Entity
@Table(name = "goal")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Goal extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "couple_id", nullable = false)
    private Long coupleId;

    // 1/5 목표 설정
    @Column(name = "target_amount")
    private Long targetAmount;

    @Column(name = "target_date")
    private LocalDate targetDate;

    @Column(name = "investment_period_months")
    private Integer investmentPeriodMonths;

    // 2/5 투자 계획
    @Column(name = "current_amount")
    private Long currentAmount;

    @Column(name = "current_amount_updated_at")
    private LocalDateTime currentAmountUpdatedAt;

    @Column(name = "monthly_investable_amount")
    private Long monthlyInvestableAmount;

    // 3/5 재무 여유
    @Enumerated(EnumType.STRING)
    @Column(name = "emergency_fund_months", length = 20)
    private EmergencyFundMonths emergencyFundMonths;

    @Enumerated(EnumType.STRING)
    @Column(name = "monthly_surplus_band", length = 20)
    private MonthlySurplusBand monthlySurplusBand;

    // 4/5 위험 반응
    @Column(name = "max_allowed_loss_rate")
    private Integer maxAllowedLossRate;

    @Enumerated(EnumType.STRING)
    @Column(name = "loss_reaction", length = 20)
    private LossReaction lossReaction;

    // 5/5 투자 경험
    @Enumerated(EnumType.STRING)
    @Column(name = "investment_experience", length = 20)
    private InvestmentExperience investmentExperience;

    // 산출 결과
    @Column(name = "risk_profile_score")
    private Integer riskProfileScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "joint_risk_profile_type", length = 30)
    private RiskProfileType jointRiskProfileType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30)
    private GoalStatus status;

    @SuppressWarnings("java:S107") // 온보딩 필드가 많아 파라미터 수가 큼 — 팩토리에서만 사용
    private Goal(Long coupleId, Long targetAmount, LocalDate targetDate, Integer investmentPeriodMonths,
                Long currentAmount, Long monthlyInvestableAmount,
                EmergencyFundMonths emergencyFundMonths, MonthlySurplusBand monthlySurplusBand,
                Integer maxAllowedLossRate, LossReaction lossReaction,
                InvestmentExperience investmentExperience,
                Integer riskProfileScore, RiskProfileType jointRiskProfileType) {
        this.coupleId = coupleId;
        this.targetAmount = targetAmount;
        this.targetDate = targetDate;
        this.investmentPeriodMonths = investmentPeriodMonths;
        this.currentAmount = currentAmount;
        this.currentAmountUpdatedAt = LocalDateTime.now();
        this.monthlyInvestableAmount = monthlyInvestableAmount;
        this.emergencyFundMonths = emergencyFundMonths;
        this.monthlySurplusBand = monthlySurplusBand;
        this.maxAllowedLossRate = maxAllowedLossRate;
        this.lossReaction = lossReaction;
        this.investmentExperience = investmentExperience;
        this.riskProfileScore = riskProfileScore;
        this.jointRiskProfileType = jointRiskProfileType;
        this.status = GoalStatus.ACTIVE;
    }

    @SuppressWarnings("java:S107")
    public static Goal create(Long coupleId, Long targetAmount, LocalDate targetDate,
                              Integer investmentPeriodMonths, Long currentAmount,
                              Long monthlyInvestableAmount, EmergencyFundMonths emergencyFundMonths,
                              MonthlySurplusBand monthlySurplusBand, Integer maxAllowedLossRate,
                              LossReaction lossReaction, InvestmentExperience investmentExperience,
                              Integer riskProfileScore, RiskProfileType jointRiskProfileType) {
        return new Goal(coupleId, targetAmount, targetDate, investmentPeriodMonths, currentAmount,
                monthlyInvestableAmount, emergencyFundMonths, monthlySurplusBand, maxAllowedLossRate,
                lossReaction, investmentExperience, riskProfileScore, jointRiskProfileType);
    }

    public boolean isActive() {
        return status == GoalStatus.ACTIVE;
    }

    public boolean isCancelled() {
        return status == GoalStatus.CANCELLED;
    }

    /** 1·2단계 목표/투자 계획 값 갱신 (점수와 무관). null 은 무시. */
    public void updatePlan(Long targetAmount, LocalDate targetDate, Long currentAmount,
                           Long monthlyInvestableAmount) {
        if (targetAmount != null) {
            this.targetAmount = targetAmount;
        }
        if (targetDate != null) {
            this.targetDate = targetDate;
        }
        if (currentAmount != null) {
            this.currentAmount = currentAmount;
            this.currentAmountUpdatedAt = LocalDateTime.now();
        }
        if (monthlyInvestableAmount != null) {
            this.monthlyInvestableAmount = monthlyInvestableAmount;
        }
    }

    /** 3~5단계 점수 문항 갱신. null 은 무시. */
    public void updateAnswers(EmergencyFundMonths emergencyFundMonths, MonthlySurplusBand monthlySurplusBand,
                              Integer maxAllowedLossRate, LossReaction lossReaction,
                              InvestmentExperience investmentExperience) {
        if (emergencyFundMonths != null) {
            this.emergencyFundMonths = emergencyFundMonths;
        }
        if (monthlySurplusBand != null) {
            this.monthlySurplusBand = monthlySurplusBand;
        }
        if (maxAllowedLossRate != null) {
            this.maxAllowedLossRate = maxAllowedLossRate;
        }
        if (lossReaction != null) {
            this.lossReaction = lossReaction;
        }
        if (investmentExperience != null) {
            this.investmentExperience = investmentExperience;
        }
    }

    /** 산출 결과 재적용 (period / R / type). */
    public void applyRiskProfile(int investmentPeriodMonths, int riskProfileScore, RiskProfileType type) {
        this.investmentPeriodMonths = investmentPeriodMonths;
        this.riskProfileScore = riskProfileScore;
        this.jointRiskProfileType = type;
    }

    /** 진척 금액 갱신. 목표 도달 시 {@code ACHIEVED} 로 자동 전환. */
    public void updateCurrentAmount(long currentAmount) {
        this.currentAmount = currentAmount;
        this.currentAmountUpdatedAt = LocalDateTime.now();
        if (targetAmount != null && currentAmount >= targetAmount) {
            this.status = GoalStatus.ACHIEVED;
        } else if (status == GoalStatus.ACHIEVED) {
            this.status = GoalStatus.ACTIVE;
        }
    }

    /** 달성/활성 목표를 다시 활성으로 (마이페이지에서 새 목표 금액·날짜 재설정 시). */
    public void reactivate() {
        if (status == GoalStatus.ACHIEVED) {
            this.status = GoalStatus.ACTIVE;
        }
    }

    public void cancel() {
        this.status = GoalStatus.CANCELLED;
    }
}
