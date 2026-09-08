package com.moait.moai.domain.analysis.dto;

import com.moait.moai.domain.analysis.dto.RiskAssessmentRequestDTO.InvestmentExperience;
import com.moait.moai.domain.analysis.dto.RiskAssessmentRequestDTO.LossReaction;
import com.moait.moai.domain.analysis.dto.RiskAssessmentRequestDTO.LossTolerance;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record JointRiskAssessmentRequestDTO(
        @NotNull LossTolerance lossTolerance,
        @NotNull LossReaction lossReaction,
        @NotNull @PositiveOrZero Integer emergencyFundMonths,
        @NotNull @PositiveOrZero BigDecimal monthlyNetIncome,
        @NotNull @PositiveOrZero BigDecimal essentialExpenses,
        @NotNull @PositiveOrZero BigDecimal debtRepayment,
        @NotNull InvestmentExperience investmentExperience) {
}
