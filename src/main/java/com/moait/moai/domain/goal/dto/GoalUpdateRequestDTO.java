package com.moait.moai.domain.goal.dto;

import com.moait.moai.common.enums.EmergencyFundMonths;
import com.moait.moai.common.enums.InvestmentExperience;
import com.moait.moai.common.enums.LossReaction;
import com.moait.moai.common.enums.MonthlySurplusBand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDate;

/**
 * 마이페이지 - 온보딩 답변 부분 수정. 보낸 필드만 갱신.
 * 점수 문항({@code emergencyFundMonths} {@code monthlySurplusBand} {@code maxAllowedLossRate}
 * {@code lossReaction} {@code investmentExperience}) 또는 {@code targetDate} 변경 시 R 자동 재계산.
 */
@Schema(example = """
        { "targetDate": "2033-09-08", "maxAllowedLossRate": 20, "lossReaction": "BUY_MORE" }""")
public record GoalUpdateRequestDTO(

        @Positive Long targetAmount,
        LocalDate targetDate,
        @PositiveOrZero Long currentAmount,
        @PositiveOrZero Long monthlyInvestableAmount,
        EmergencyFundMonths emergencyFundMonths,
        MonthlySurplusBand monthlySurplusBand,
        Integer maxAllowedLossRate,
        LossReaction lossReaction,
        InvestmentExperience investmentExperience
) {

    /** 점수/투자기간 재계산이 필요한 필드가 하나라도 왔는지. */
    public boolean touchesRiskInputs() {
        return targetDate != null
                || emergencyFundMonths != null
                || monthlySurplusBand != null
                || maxAllowedLossRate != null
                || lossReaction != null
                || investmentExperience != null;
    }
}
