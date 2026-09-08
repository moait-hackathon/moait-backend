package com.moait.moai.domain.analysis.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record InvestmentAgreementRequestDTO(
        @NotNull @Positive Long goalId,
        @NotNull @Valid RiskAssessmentRequestDTO personA,
        @NotNull @Valid RiskAssessmentRequestDTO personB,
        @NotNull @Valid JointRiskAssessmentRequestDTO jointFund,
        @NotNull @Valid GoalAnalysisRequestDTO goal) {
}
