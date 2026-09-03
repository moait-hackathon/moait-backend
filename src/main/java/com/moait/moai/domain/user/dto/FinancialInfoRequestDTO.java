package com.moait.moai.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(example = """
        { "annualIncome": 45000000, "totalAsset": 30000000 }""")
public record FinancialInfoRequestDTO(

        @NotNull
        @PositiveOrZero
        Long annualIncome,

        @NotNull
        @PositiveOrZero
        Long totalAsset
) {
}
