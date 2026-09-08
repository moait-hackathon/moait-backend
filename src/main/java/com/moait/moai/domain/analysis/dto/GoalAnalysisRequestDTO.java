package com.moait.moai.domain.analysis.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDate;

public record GoalAnalysisRequestDTO(
        @NotNull @Positive Long targetAmount,
        @NotNull @PositiveOrZero Long currentAmount,
        @NotNull @PositiveOrZero Long monthlyContribution,
        @NotNull @JsonFormat(pattern = "yyyy-MM-dd") LocalDate targetDate) {
}
