package com.moait.moai.domain.analysis.service;

import com.moait.moai.common.exception.BusinessException;
import com.moait.moai.common.exception.ErrorCode;
import com.moait.moai.domain.analysis.dto.GoalAnalysisRequestDTO;
import com.moait.moai.domain.analysis.dto.InvestmentAgreementRequestDTO;
import com.moait.moai.domain.analysis.dto.InvestmentAgreementResponseDTO;
import com.moait.moai.domain.analysis.dto.InvestmentAgreementResponseDTO.GoalRequirement;
import com.moait.moai.domain.analysis.dto.InvestmentAgreementResponseDTO.Recommendation;
import com.moait.moai.domain.analysis.dto.InvestmentAgreementResponseDTO.RiskScore;
import com.moait.moai.domain.analysis.dto.InvestmentAnalysisInputDTO.JointFund;
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
    private final InvestmentAnalysisInputService inputService;
    private final PortfolioRiskCalculator portfolioRiskCalculator;

    @Override
    public InvestmentAgreementResponseDTO analyze(InvestmentAgreementRequestDTO request) {
        var input = inputService.load(request.userId());
        RiskScore a = portfolioRiskCalculator.calculate(input.personA(), "A");
        RiskScore b = portfolioRiskCalculator.calculate(input.personB(), "B");
        GoalRequirement goal = calculateGoal(input.goal());
        RiskScore c = scoreJoint(input.jointFund(), goal.investmentMonths());

        BigDecimal weighted = BigDecimal.valueOf(a.preferenceScore() + b.preferenceScore()
                + c.preferenceScore() * 3L).divide(BigDecimal.valueOf(5));
        // 보유 자산으로 개인 손실 허용 상한을 추정하지 않는다. 공동 설문의 상한을 적용한다.
        int finalMax = c.finalLimit();
        BigDecimal center = weighted.min(BigDecimal.valueOf(finalMax));
        Recommendation recommendation = new Recommendation(
                weighted, center, Math.max(0, center.intValue() - 5),
                Math.min(finalMax, center.intValue() + 5), finalMax);
        String status = determineStatus(recommendation, goal);

        InvestmentAgreementResponseDTO result = new InvestmentAgreementResponseDTO(a, b, c, recommendation, goal,
                agreementGenerator.generate(recommendation, goal, input.goal(), status));
        reportRepository.save(InvestmentReport.from(input.goalId(), input.goal(), result));
        return result;
    }

    static RiskScore scoreJoint(JointFund q, int months) {
        int loss = switch (q.maxAllowedLossRate()) {
            case 0 -> 0; case 5 -> 6; case 10 -> 12;
            case 20 -> 19; case 30 -> 23; case 40 -> 25;
            default -> throw new IllegalArgumentException("Unsupported loss rate");
        };
        int reaction = switch (q.lossReaction()) {
            case SELL_ALL -> 0; case SELL_MOST -> 3; case SELL_PART -> 7;
            case HOLD -> 11; case BUY_MORE -> 15;
        };
        int emergency = switch (q.emergencyFundBand()) {
            case UNDER_1M -> 0; case M1_3 -> 4; case M3_6 -> 9;
            case M6_12 -> 14; case OVER_12M -> 18;
        };
        int surplus = switch (q.surplusBand()) {
            case UNDER_0 -> 0; case UNDER_10 -> 4; case B10_20 -> 8;
            case B20_30 -> 13; case OVER_30 -> 17;
        };
        int horizon = months < 12 ? 0 : months < 24 ? 3 : months < 36 ? 6
                : months < 60 ? 9 : months < 120 ? 12 : 15;
        int experience = switch (q.investmentExperience()) {
            case NONE -> 0; case SAVINGS_ONLY -> 2; case ETF_ONLY -> 5;
            case STOCK_ALL -> 8; case MULTI_ASSET -> 10;
        };
        int preference = loss + reaction + emergency + surplus + horizon + experience;
        int userLimit = switch (q.maxAllowedLossRate()) {
            case 0 -> 20; case 5 -> 30; case 10 -> 45;
            case 20 -> 65; case 30 -> 80; case 40 -> 100;
            default -> throw new IllegalArgumentException("Unsupported loss rate");
        };
        int serviceLimit = months < 12 ? 20 : months < 24 ? 40 : months < 36 ? 55
                : months < 60 ? 70 : months < 120 ? 85 : 100;
        int reactionLimit = switch (q.lossReaction()) {
            case SELL_ALL -> 20; case SELL_MOST -> 30; case SELL_PART -> 45;
            case HOLD -> 70; case BUY_MORE -> 90;
        };
        userLimit = Math.min(userLimit, reactionLimit);
        int emergencyLimit = switch (q.emergencyFundBand()) {
            case UNDER_1M -> 20; case M1_3 -> 30; case M3_6 -> 50;
            case M6_12, OVER_12M -> 100;
        };
        serviceLimit = Math.min(serviceLimit, emergencyLimit);
        return new RiskScore(preference, userLimit, serviceLimit,
                Math.min(userLimit, serviceLimit), profileType(preference));
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

    static String profileType(int score) {
        if (score <= 20) return "STABLE";
        if (score <= 40) return "STABLE_SEEKING";
        if (score <= 60) return "NEUTRAL";
        if (score <= 80) return "ACTIVE";
        return "AGGRESSIVE";
    }
}
