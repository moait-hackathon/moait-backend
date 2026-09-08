package com.moait.moai.domain.analysis.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record InvestmentAgreementRequestDTO(
        @NotNull @Positive Long userId) {
}
