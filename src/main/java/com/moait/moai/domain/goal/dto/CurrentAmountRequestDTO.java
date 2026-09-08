package com.moait.moai.domain.goal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(example = """
        { "currentAmount": 15000000 }""")
public record CurrentAmountRequestDTO(

        @NotNull @PositiveOrZero Long currentAmount
) {
}
