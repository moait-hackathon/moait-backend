package com.moait.moai.domain.analysis.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDate;

public record GoalAnalysisRequestDTO(
        @NotNull @Positive Long targetAmount,
        @NotNull @PositiveOrZero Long currentAmount,
        @NotNull @PositiveOrZero Long monthlyContribution,
        @NotNull @PositiveOrZero Long additionalDeposit,
        @NotNull @PositiveOrZero Long plannedWithdrawal,
        @NotNull @PositiveOrZero Long estimatedFeesAndTaxes,
        @NotNull LocalDate targetDate) {
}
