package com.moait.moai.domain.analysis.service;

import com.moait.moai.common.exception.BusinessException;
import com.moait.moai.common.exception.ErrorCode;
import com.moait.moai.domain.analysis.dto.GoalAnalysisRequestDTO;
import com.moait.moai.domain.analysis.dto.InvestmentAgreementRequestDTO;
import com.moait.moai.domain.analysis.dto.InvestmentAgreementResponseDTO;
import com.moait.moai.domain.analysis.dto.InvestmentAgreementResponseDTO.GoalRequirement;
import com.moait.moai.domain.analysis.dto.InvestmentAgreementResponseDTO.Recommendation;
import com.moait.moai.domain.analysis.dto.InvestmentAgreementResponseDTO.RiskScore;
import com.moait.moai.domain.analysis.dto.RiskAssessmentRequestDTO;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InvestmentAnalysisServiceImpl implements InvestmentAnalysisService {

    private final AgreementGenerator agreementGenerator;

    @Override
    @Transactional(readOnly = true)
    public InvestmentAgreementResponseDTO analyze(InvestmentAgreementRequestDTO request) {
        RiskScore a = score(request.personA());
        RiskScore b = score(request.personB());
        RiskScore c = score(request.jointFund());

        int weighted = (int) Math.round(a.preferenceScore() * 0.2
                + b.preferenceScore() * 0.2 + c.preferenceScore() * 0.6);
        int finalMax = Math.min(a.finalLimit(), Math.min(b.finalLimit(), c.finalLimit()));
        int center = Math.min(weighted, finalMax);
        Recommendation recommendation = new Recommendation(
                weighted, center, Math.max(0, center - 5), Math.min(finalMax, center + 5), finalMax);
        GoalRequirement goal = calculateGoal(request.goal());
        String status = determineStatus(recommendation, goal);

        return new InvestmentAgreementResponseDTO(a, b, c, recommendation, goal,
                agreementGenerator.generate(recommendation, goal, request.goal(), status));
    }

    private RiskScore score(RiskAssessmentRequestDTO q) {
        int preference = lossPreference(q) + abilityPreference(q)
                + horizonPreference(q) + experiencePreference(q);
        int userLimit = Math.min(lossLimit(q),
                Math.min(capitalLimit(q), reactionLimit(q)));
        int serviceLimit = Math.min(horizonLimit(q), Math.min(emergencyLimit(q),
                Math.min(fixedCostLimit(q.fixedCostRatio()),
                        assetRatioLimit(q.investmentAssetRatio()))));
        int finalLimit = Math.min(userLimit, serviceLimit);
        return new RiskScore(preference, userLimit, serviceLimit, finalLimit,
                profileType(preference));
    }

    private int lossPreference(RiskAssessmentRequestDTO q) {
        int loss = switch (q.lossTolerance()) {
            case NO_LOSS -> 0; case UP_TO_5 -> 3; case UP_TO_10 -> 7;
            case UP_TO_20 -> 12; case UP_TO_30, OVER_30 -> 15;
        };
        int capital = switch (q.capitalProtection()) {
            case P100 -> 0; case P95 -> 2; case P90 -> 5; case P80 -> 8; case BELOW_80 -> 10;
        };
        int reaction = switch (q.lossReaction()) {
            case SELL_ALL -> 0; case SELL_MOST -> 2; case SELL_PART -> 5;
            case HOLD -> 8; case BUY_MORE -> 10;
        };
        int burden = switch (q.psychologicalBurden()) {
            case VERY_ANXIOUS -> 0; case ANXIOUS_AT_5 -> 1;
            case TOLERATE_10 -> 3; case TOLERATE_20 -> 5;
        };
        return loss + capital + reaction + burden;
    }

    private int abilityPreference(RiskAssessmentRequestDTO q) {
        int emergency = q.emergencyFundMonths() < 1 ? 0 : q.emergencyFundMonths() < 3 ? 2
                : q.emergencyFundMonths() < 6 ? 4 : q.emergencyFundMonths() < 12 ? 6 : 8;
        int income = switch (q.incomeStability()) {
            case NONE -> 0; case HIGHLY_VARIABLE -> 2; case POSSIBLY_DECREASING -> 4;
            case STABLE -> 6; case MULTIPLE_STABLE -> 7;
        };
        int fixed = q.fixedCostRatio() > 80 ? 0 : q.fixedCostRatio() >= 60 ? 2
                : q.fixedCostRatio() >= 40 ? 4 : q.fixedCostRatio() >= 20 ? 6 : 8;
        int assets = q.investmentAssetRatio() > 80 ? 0 : q.investmentAssetRatio() >= 60 ? 2
                : q.investmentAssetRatio() >= 40 ? 3 : q.investmentAssetRatio() >= 20 ? 5 : 6;
        int expense = switch (q.plannedExpense()) {
            case MOST -> 0; case HALF_OR_MORE -> 2; case SOME -> 4; case NONE -> 6;
        };
        return emergency + income + fixed + assets + expense;
    }

    private int horizonPreference(RiskAssessmentRequestDTO q) {
        int horizon = switch (q.investmentHorizon()) {
            case UNDER_1Y -> 0; case Y1_2 -> 2; case Y2_3 -> 4;
            case Y3_5 -> 6; case Y5_10 -> 8; case OVER_10Y -> 10;
        };
        int withdrawal = switch (q.withdrawalPlan()) {
            case ANYTIME -> 0; case LIKELY_WITHIN_1Y -> 1; case UNCERTAIN -> 2;
            case FIXED_PARTIAL -> 4; case NONE -> 5;
        };
        return horizon + withdrawal;
    }

    private int experiencePreference(RiskAssessmentRequestDTO q) {
        int experience = switch (q.investmentExperience()) {
            case NONE -> 0; case SAVINGS_ONLY -> 1; case BOND_FUND_ETF -> 3;
            case STOCK -> 4; case HIGH_RISK -> 5;
        };
        return experience + q.financialKnowledgeCorrectAnswers();
    }

    private int lossLimit(RiskAssessmentRequestDTO q) {
        return switch (q.lossTolerance()) {
            case NO_LOSS -> 20; case UP_TO_5 -> 30; case UP_TO_10 -> 45;
            case UP_TO_20 -> 65; case UP_TO_30 -> 80; case OVER_30 -> 100;
        };
    }

    private int capitalLimit(RiskAssessmentRequestDTO q) {
        return switch (q.capitalProtection()) {
            case P100 -> 20; case P95 -> 30; case P90 -> 45;
            case P80 -> 65; case BELOW_80 -> 85;
        };
    }

    private int reactionLimit(RiskAssessmentRequestDTO q) {
        return switch (q.lossReaction()) {
            case SELL_ALL -> 20; case SELL_MOST -> 30; case SELL_PART -> 45;
            case HOLD -> 70; case BUY_MORE -> 90;
        };
    }

    private int horizonLimit(RiskAssessmentRequestDTO q) {
        return switch (q.investmentHorizon()) {
            case UNDER_1Y -> 20; case Y1_2 -> 40; case Y2_3 -> 55;
            case Y3_5 -> 70; case Y5_10 -> 85; case OVER_10Y -> 100;
        };
    }

    private int emergencyLimit(RiskAssessmentRequestDTO q) {
        return q.emergencyFundMonths() < 1 ? 20 : q.emergencyFundMonths() < 3 ? 30
                : q.emergencyFundMonths() < 6 ? 50 : 100;
    }

    private int fixedCostLimit(double ratio) {
        return ratio > 80 ? 20 : ratio >= 60 ? 30 : ratio >= 40 ? 50 : ratio >= 20 ? 75 : 100;
    }

    private int assetRatioLimit(double ratio) {
        return ratio > 80 ? 30 : ratio >= 60 ? 45 : ratio >= 40 ? 60 : ratio >= 20 ? 80 : 100;
    }

    private GoalRequirement calculateGoal(GoalAnalysisRequestDTO goal) {
        long months = ChronoUnit.MONTHS.between(LocalDate.now(Clock.systemDefaultZone()), goal.targetDate());
        if (months < 1) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "목표일은 현재 날짜보다 최소 1개월 이후여야 합니다.");
        }
        double targetWithCosts = goal.targetAmount() + goal.plannedWithdrawal()
                + goal.estimatedFeesAndTaxes() - goal.additionalDeposit();
        double zeroReturnValue = goal.currentAmount() + goal.monthlyContribution() * months;
        double annualRate = zeroReturnValue >= targetWithCosts ? 0.0
                : solveAnnualRate(goal.currentAmount(), goal.monthlyContribution(), months, targetWithCosts);
        double percent = Math.round(annualRate * 1000.0) / 10.0;
        int[] range = goalRange(percent);
        return new GoalRequirement(percent, range[0], range[1], percent <= 20.0);
    }

    private double solveAnnualRate(long present, long monthly, long months, double target) {
        double low = 0.0;
        double high = 10.0;
        for (int i = 0; i < 200; i++) {
            double mid = (low + high) / 2.0;
            double monthlyRate = Math.pow(1.0 + mid, 1.0 / 12.0) - 1.0;
            double future = present * Math.pow(1.0 + monthlyRate, months)
                    + monthly * (Math.pow(1.0 + monthlyRate, months) - 1.0) / monthlyRate;
            if (future < target) low = mid; else high = mid;
        }
        return high;
    }

    private int[] goalRange(double rate) {
        if (rate <= 2) return new int[]{0, 20};
        if (rate <= 4) return new int[]{20, 35};
        if (rate <= 6) return new int[]{35, 50};
        if (rate <= 8) return new int[]{50, 65};
        if (rate <= 12) return new int[]{65, 80};
        if (rate <= 20) return new int[]{80, 95};
        return new int[]{100, 100};
    }

    private String determineStatus(Recommendation r, GoalRequirement g) {
        if (g.rangeMax() < r.rangeMin()) return "LOWER_RISK_SUFFICIENT";
        if (g.rangeMin() <= r.rangeMax() && g.rangeMax() >= r.rangeMin()) return "SUITABLE";
        if (g.rangeMin() <= r.finalMax()) return "CONDITIONAL";
        return "UNSUITABLE";
    }

    private String profileType(int score) {
        if (score <= 20) return "STABLE";
        if (score <= 40) return "STABLE_SEEKING";
        if (score <= 60) return "NEUTRAL";
        if (score <= 80) return "ACTIVE";
        return "AGGRESSIVE";
    }
}
