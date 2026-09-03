package com.moait.moai.domain.analysis.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record RiskAssessmentRequestDTO(
        @NotNull LossTolerance lossTolerance,
        @NotNull CapitalProtection capitalProtection,
        @NotNull LossReaction lossReaction,
        @NotNull PsychologicalBurden psychologicalBurden,
        @NotNull @Min(0) Integer emergencyFundMonths,
        @NotNull IncomeStability incomeStability,
        @NotNull @DecimalMin("0.0") @DecimalMax("100.0") Double fixedCostRatio,
        @NotNull @DecimalMin("0.0") @DecimalMax("100.0") Double investmentAssetRatio,
        @NotNull PlannedExpense plannedExpense,
        @NotNull InvestmentHorizon investmentHorizon,
        @NotNull WithdrawalPlan withdrawalPlan,
        @NotNull InvestmentExperience investmentExperience,
        @NotNull @Min(0) @Max(5) Integer financialKnowledgeCorrectAnswers) {

    public enum LossTolerance { NO_LOSS, UP_TO_5, UP_TO_10, UP_TO_20, UP_TO_30, OVER_30 }
    public enum CapitalProtection { P100, P95, P90, P80, BELOW_80 }
    public enum LossReaction { SELL_ALL, SELL_MOST, SELL_PART, HOLD, BUY_MORE }
    public enum PsychologicalBurden { VERY_ANXIOUS, ANXIOUS_AT_5, TOLERATE_10, TOLERATE_20 }
    public enum IncomeStability { NONE, HIGHLY_VARIABLE, POSSIBLY_DECREASING, STABLE, MULTIPLE_STABLE }
    public enum PlannedExpense { MOST, HALF_OR_MORE, SOME, NONE }
    public enum InvestmentHorizon { UNDER_1Y, Y1_2, Y2_3, Y3_5, Y5_10, OVER_10Y }
    public enum WithdrawalPlan { ANYTIME, LIKELY_WITHIN_1Y, UNCERTAIN, FIXED_PARTIAL, NONE }
    public enum InvestmentExperience { NONE, SAVINGS_ONLY, BOND_FUND_ETF, STOCK, HIGH_RISK }
}
