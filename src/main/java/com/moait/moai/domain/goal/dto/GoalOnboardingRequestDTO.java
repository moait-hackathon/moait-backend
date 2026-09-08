package com.moait.moai.domain.goal.dto;

import com.moait.moai.common.enums.EmergencyFundMonths;
import com.moait.moai.common.enums.InvestmentExperience;
import com.moait.moai.common.enums.LossReaction;
import com.moait.moai.common.enums.MonthlySurplusBand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDate;

/** 온보딩 5단계 제출. 모든 필드 필수. {@code maxAllowedLossRate} 는 0/5/10/20/30/40 만 유효(서비스 검증). */
@Schema(example = """
        {
          "targetAmount": 100000000,
          "targetDate": "2031-09-08",
          "currentAmount": 20000000,
          "monthlyInvestableAmount": 500000,
          "emergencyFundMonths": "M3_6",
          "monthlySurplusBand": "B20_30",
          "maxAllowedLossRate": 10,
          "lossReaction": "HOLD",
          "investmentExperience": "ETF_ONLY"
        }""")
public record GoalOnboardingRequestDTO(

        @NotNull @Positive Long targetAmount,
        @NotNull LocalDate targetDate,
        @NotNull @PositiveOrZero Long currentAmount,
        @NotNull @PositiveOrZero Long monthlyInvestableAmount,
        @NotNull EmergencyFundMonths emergencyFundMonths,
        @NotNull MonthlySurplusBand monthlySurplusBand,
        @NotNull Integer maxAllowedLossRate,
        @NotNull LossReaction lossReaction,
        @NotNull InvestmentExperience investmentExperience
) {
}
