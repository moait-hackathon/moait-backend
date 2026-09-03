package com.moait.moai.domain.analysis.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record InvestmentAgreementRequestDTO(
        @NotNull @Valid RiskAssessmentRequestDTO personA,
        @NotNull @Valid RiskAssessmentRequestDTO personB,
        @NotNull @Valid RiskAssessmentRequestDTO jointFund,
        @NotNull @Valid GoalAnalysisRequestDTO goal) {
}
