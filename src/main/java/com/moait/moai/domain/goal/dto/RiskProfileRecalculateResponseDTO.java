package com.moait.moai.domain.goal.dto;

import com.moait.moai.common.enums.RiskProfileType;
import com.moait.moai.domain.goal.entity.Goal;

public record RiskProfileRecalculateResponseDTO(
        Long goalId,
        Integer investmentPeriodMonths,
        Integer riskProfileScore,
        RiskProfileType jointRiskProfileType,
        String jointRiskProfileTypeLabel
) {

    public static RiskProfileRecalculateResponseDTO of(Goal goal) {
        RiskProfileType type = goal.getJointRiskProfileType();
        return new RiskProfileRecalculateResponseDTO(
                goal.getId(), goal.getInvestmentPeriodMonths(), goal.getRiskProfileScore(),
                type, type == null ? null : type.getLabel());
    }
}
