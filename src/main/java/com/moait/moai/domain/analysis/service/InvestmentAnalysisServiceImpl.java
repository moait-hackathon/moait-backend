package com.moait.moai.domain.analysis.service;

import com.moait.moai.common.exception.BusinessException;
import com.moait.moai.common.exception.ErrorCode;
import com.moait.moai.domain.analysis.dto.GoalAnalysisRequestDTO;
import com.moait.moai.domain.analysis.dto.InvestmentAgreementRequestDTO;
import com.moait.moai.domain.analysis.dto.InvestmentAgreementResponseDTO;
import com.moait.moai.domain.analysis.dto.InvestmentAgreementResponseDTO.GoalRequirement;
import com.moait.moai.domain.analysis.dto.InvestmentAgreementResponseDTO.Recommendation;
import com.moait.moai.domain.analysis.dto.InvestmentAgreementResponseDTO.RiskScore;
import com.moait.moai.domain.analysis.dto.JointRiskAssessmentRequestDTO;
import com.moait.moai.domain.analysis.dto.RiskAssessmentRequestDTO;
import com.moait.moai.domain.report.entity.InvestmentReport;
import com.moait.moai.domain.report.repository.InvestmentReportRepository;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InvestmentAnalysisServiceImpl implements InvestmentAnalysisService {

    private final AgreementGenerator agreementGenerator;
    private final InvestmentReportRepository reportRepository;

    @Override
    public InvestmentAgreementResponseDTO analyze(InvestmentAgreementRequestDTO request) {
        if (reportRepository.countGoal(request.goalId()) == 0) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                    "공동 목표를 찾을 수 없습니다.");
        }
        RiskScore a = score(request.personA());
        RiskScore b = score(request.personB());
        GoalRequirement goal = calculateGoal(request.goal());
        RiskScore c = scoreJoint(request.jointFund(), goal.investmentMonths());

        BigDecimal weighted = BigDecimal.valueOf(a.preferenceScore() + b.preferenceScore()
                + c.preferenceScore() * 3L).divide(BigDecimal.valueOf(5));
        int finalMax = Math.min(a.finalLimit(), Math.min(b.finalLimit(), c.finalLimit()));
        BigDecimal center = weighted.min(BigDecimal.valueOf(finalMax));
        Recommendation recommendation = new Recommendation(
                weighted, center, Math.max(0, center.intValue() - 5),
                Math.min(finalMax, center.intValue() + 5), finalMax);
        String status = determineStatus(recommendation, goal);

        InvestmentAgreementResponseDTO result = new InvestmentAgreementResponseDTO(a, b, c, recommendation, goal,
                agreementGenerator.generate(recommendation, goal, request.goal(), status));
        reportRepository.save(InvestmentReport.from(request.goalId(), request.goal(), result));
        return result;
    }

    static RiskScore scoreJoint(JointRiskAssessmentRequestDTO q, int months) {
        int loss = switch (q.lossTolerance()) {
            case NO_LOSS -> 0; case UP_TO_5 -> 6; case UP_TO_10 -> 12;
            case UP_TO_20 -> 19; case UP_TO_30 -> 23; case OVER_30 -> 25;
        };
        int reaction = switch (q.lossReaction()) {
            case SELL_ALL -> 0; case SELL_MOST -> 3; case SELL_PART -> 7;
            case HOLD -> 11; case BUY_MORE -> 15;
        };
        int emergency = q.emergencyFundMonths() < 1 ? 0 : q.emergencyFundMonths() < 3 ? 4
                : q.emergencyFundMonths() < 6 ? 9 : q.emergencyFundMonths() < 12 ? 14 : 18;
        BigDecimal available = q.monthlyNetIncome().subtract(q.essentialExpenses()).subtract(q.debtRepayment());
        int surplus = q.monthlyNetIncome().signum() == 0 || available.signum() <= 0 ? 0
                : available.compareTo(q.monthlyNetIncome().multiply(new BigDecimal("0.10"))) < 0 ? 4
                : available.compareTo(q.monthlyNetIncome().multiply(new BigDecimal("0.20"))) < 0 ? 8
                : available.compareTo(q.monthlyNetIncome().multiply(new BigDecimal("0.30"))) < 0 ? 13 : 17;
        int horizon = months < 12 ? 0 : months < 24 ? 3 : months < 36 ? 6
                : months < 60 ? 9 : months < 120 ? 12 : 15;
        int experience = switch (q.investmentExperience()) {
            case NONE -> 0; case SAVINGS_ONLY -> 2; case BOND_FUND_ETF -> 5;
            case STOCK -> 8; case HIGH_RISK -> 10;
        };
        int preference = loss + reaction + emergency + surplus + horizon + experience;
        int userLimit = switch (q.lossTolerance()) {
            case NO_LOSS -> 20; case UP_TO_5 -> 30; case UP_TO_10 -> 45;
            case UP_TO_20 -> 65; case UP_TO_30 -> 80; case OVER_30 -> 100;
        };
        int serviceLimit = months < 12 ? 20 : months < 24 ? 40 : months < 36 ? 55
                : months < 60 ? 70 : months < 120 ? 85 : 100;
        int reactionLimit = switch (q.lossReaction()) {
            case SELL_ALL -> 20; case SELL_MOST -> 30; case SELL_PART -> 45;
            case HOLD -> 70; case BUY_MORE -> 90;
        };
        userLimit = Math.min(userLimit, reactionLimit);
        int emergencyLimit = q.emergencyFundMonths() < 1 ? 20 : q.emergencyFundMonths() < 3 ? 30
                : q.emergencyFundMonths() < 6 ? 50 : 100;
        serviceLimit = Math.min(serviceLimit, emergencyLimit);
        return new RiskScore(preference, userLimit, serviceLimit,
                Math.min(userLimit, serviceLimit), profileType(preference));
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
        if (months < 1 || months > 1200) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "목표일은 현재 날짜보다 1개월 이상 100년 이내여야 합니다.");
        }
        BigDecimal target = BigDecimal.valueOf(goal.targetAmount());
        BigDecimal present = BigDecimal.valueOf(goal.currentAmount());
        BigDecimal monthly = BigDecimal.valueOf(goal.monthlyContribution());
        BigDecimal zeroReturn = present.add(monthly.multiply(BigDecimal.valueOf(months)));
        if (zeroReturn.compareTo(target) >= 0) {
            return new GoalRequirement(0.0, 0, 20, true, (int) months, "LEGACY_RETURN_BANDS");
        }
        if (present.signum() == 0 && (monthly.signum() == 0 || months == 1)) {
            return new GoalRequirement(null, 100, 100, false, (int) months, "LEGACY_RETURN_BANDS");
        }
        BigDecimal low = BigDecimal.ZERO;
        BigDecimal high = target.divide(present.signum() > 0 ? present : monthly,
                MathContext.DECIMAL128).max(BigDecimal.ONE);
        for (int i = 0; i < 200; i++) {
            BigDecimal mid = low.add(high).divide(BigDecimal.valueOf(2), MathContext.DECIMAL128);
            BigDecimal growth = BigDecimal.ONE.add(mid).pow((int) months, MathContext.DECIMAL128);
            BigDecimal future = present.multiply(growth).add(monthly.multiply(
                    growth.subtract(BigDecimal.ONE).divide(mid, MathContext.DECIMAL128)));
            if (future.compareTo(target) < 0) low = mid; else high = mid;
        }
        double percent = BigDecimal.ONE.add(high).pow(12, MathContext.DECIMAL128)
                .subtract(BigDecimal.ONE).multiply(BigDecimal.valueOf(100)).doubleValue();
        int[] range = goalRange(percent);
        return new GoalRequirement(BigDecimal.valueOf(percent).setScale(1, RoundingMode.HALF_UP).doubleValue(),
                range[0], range[1], percent <= 20.0, (int) months, "LEGACY_RETURN_BANDS");
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

    static String determineStatus(Recommendation r, GoalRequirement g) {
        if (!g.realistic() || g.rangeMin() > r.finalMax()) return "UNSUITABLE";
        BigDecimal difference = BigDecimal.valueOf(g.rangeMin()).subtract(r.centerScore());
        if (difference.compareTo(BigDecimal.valueOf(-15)) <= 0) return "LOWER_RISK_SUFFICIENT";
        if (difference.compareTo(BigDecimal.valueOf(-5)) < 0) return "GOAL_INCREASE_POSSIBLE";
        if (difference.compareTo(BigDecimal.valueOf(5)) <= 0) return "SUITABLE";
        if (difference.compareTo(BigDecimal.valueOf(15)) < 0) return "CONDITIONAL";
        return "UNSUITABLE";
    }

    private static String profileType(int score) {
        if (score <= 20) return "STABLE";
        if (score <= 40) return "STABLE_SEEKING";
        if (score <= 60) return "NEUTRAL";
        if (score <= 80) return "ACTIVE";
        return "AGGRESSIVE";
    }
}
