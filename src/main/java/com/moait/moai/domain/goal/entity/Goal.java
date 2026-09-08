package com.moait.moai.domain.goal.entity;

import com.moait.moai.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 공동 목표에 저장된 분석 입력. 스키마 기준은 MoAItDB.sql이다. */
@Entity
@Table(name = "goal")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Goal extends BaseTimeEntity {
    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "couple_id", nullable = false)
    private Long coupleId;

    @Column(name = "target_amount")
    private Long targetAmount;

    @Column(name = "target_date")
    private LocalDate targetDate;

    @Column(name = "current_amount")
    private Long currentAmount;

    @Column(name = "monthly_investable_amount")
    private Long monthlyInvestableAmount;

    @Column(name = "emergency_fund_months", length = 20)
    private String emergencyFundMonths;

    @Column(name = "monthly_surplus_band", length = 20)
    private String monthlySurplusBand;

    @Column(name = "max_allowed_loss_rate")
    private Integer maxAllowedLossRate;

    @Column(name = "loss_reaction", length = 20)
    private String lossReaction;

    @Column(name = "investment_experience", length = 20)
    private String investmentExperience;

    @Column(name = "status", length = 30)
    private String status;
}
