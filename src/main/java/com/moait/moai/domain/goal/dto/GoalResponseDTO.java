package com.moait.moai.domain.goal.dto;

import com.moait.moai.common.enums.EmergencyFundMonths;
import com.moait.moai.common.enums.GoalStatus;
import com.moait.moai.common.enums.InvestmentExperience;
import com.moait.moai.common.enums.LossReaction;
import com.moait.moai.common.enums.MonthlySurplusBand;
import com.moait.moai.common.enums.OnboardingStep;
import com.moait.moai.common.enums.RiskProfileType;
import com.moait.moai.domain.goal.entity.Goal;
import com.moait.moai.domain.goal.service.GoalProgress;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 공동 목표 생성/조회/수정 공통 응답. */
public record GoalResponseDTO(
        Long goalId,
        Long coupleId,
        Long targetAmount,
        LocalDate targetDate,
        Long currentAmount,
        LocalDateTime currentAmountUpdatedAt,
        Long monthlyInvestableAmount,
        Integer investmentPeriodMonths,
        EmergencyFundMonths emergencyFundMonths,
        MonthlySurplusBand monthlySurplusBand,
        Integer maxAllowedLossRate,
        LossReaction lossReaction,
        InvestmentExperience investmentExperience,
        Integer riskProfileScore,
        RiskProfileType jointRiskProfileType,
        String jointRiskProfileTypeLabel,
        GoalStatus status,
        GoalProgress progress,
        Boolean hasReport,
        OnboardingStep onboardingStep,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static GoalResponseDTO of(Goal goal, GoalProgress progress, boolean hasReport,
                                     OnboardingStep onboardingStep) {
        RiskProfileType type = goal.getJointRiskProfileType();
        return new GoalResponseDTO(
                goal.getId(),
                goal.getCoupleId(),
                goal.getTargetAmount(),
                goal.getTargetDate(),
                goal.getCurrentAmount(),
                goal.getCurrentAmountUpdatedAt(),
                goal.getMonthlyInvestableAmount(),
                goal.getInvestmentPeriodMonths(),
                goal.getEmergencyFundMonths(),
                goal.getMonthlySurplusBand(),
                goal.getMaxAllowedLossRate(),
                goal.getLossReaction(),
                goal.getInvestmentExperience(),
                goal.getRiskProfileScore(),
                type,
                type == null ? null : type.getLabel(),
                goal.getStatus(),
                progress,
                hasReport,
                onboardingStep,
                goal.getCreatedAt(),
                goal.getUpdatedAt());
    }
}
