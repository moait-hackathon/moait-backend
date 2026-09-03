package com.moait.moai.domain.user.dto;

import com.moait.moai.common.enums.HoldingAssetType;
import com.moait.moai.common.enums.InvestmentExperience;
import com.moait.moai.common.enums.InvestmentHorizon;
import com.moait.moai.common.enums.LossReaction;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;

@Schema(example = """
        {
          "investmentExperience": "ETF_ONLY",
          "lossReaction": "HOLD",
          "maxTolerableLossRate": 10,
          "investmentHorizon": "Y3_5",
          "holdingAssets": [
            { "type": "DEPOSIT", "amount": 10000000 },
            { "type": "ETF", "amount": 5000000 }
          ],
          "monthlyInvestableAmount": 500000,
          "emergencyFundSecured": true
        }""")
public record InvestmentProfileRequestDTO(

        @NotNull
        InvestmentExperience investmentExperience,

        @NotNull
        LossReaction lossReaction,

        @NotNull
        @Min(0)
        @Max(100)
        Integer maxTolerableLossRate,

        @NotNull
        InvestmentHorizon investmentHorizon,

        @NotNull
        @Valid
        List<HoldingAssetItem> holdingAssets,

        @NotNull
        @PositiveOrZero
        Long monthlyInvestableAmount,

        @NotNull
        Boolean emergencyFundSecured
) {

    public record HoldingAssetItem(
            @NotNull HoldingAssetType type,
            @NotNull @PositiveOrZero Long amount
    ) {
    }
}
