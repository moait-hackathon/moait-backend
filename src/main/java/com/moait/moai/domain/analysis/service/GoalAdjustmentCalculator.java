package com.moait.moai.domain.analysis.service;

import com.moait.moai.domain.analysis.dto.GoalAnalysisRequestDTO;
import com.moait.moai.domain.analysis.dto.InvestmentAgreementResponseDTO.GoalRequirement;
import com.moait.moai.domain.analysis.dto.InvestmentAgreementResponseDTO.Recommendation;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Component;

/** LLM에 전달할 목표 조정 수치를 월말 납입 미래가치로 계산한다. */
@Component
public class GoalAdjustmentCalculator {
    private static final MathContext MC = MathContext.DECIMAL128;
    private static final int MAX_MONTHS = 1200;

    public List<String> calculate(Recommendation r, GoalRequirement g, GoalAnalysisRequestDTO goal) {
        int months = g.investmentMonths();
        BigDecimal annualPercent = assumedAnnualPercent(r);
        BigDecimal factor = monthlyFactor(annualPercent);
        BigDecimal present = BigDecimal.valueOf(goal.currentAmount());
        BigDecimal monthly = BigDecimal.valueOf(goal.monthlyContribution());
        BigDecimal target = BigDecimal.valueOf(goal.targetAmount());
        BigDecimal growth = factor.pow(months, MC);
        BigDecimal annuity = annuity(factor, months);
        BigDecimal requiredMonthly = target.subtract(present.multiply(growth, MC))
                .max(BigDecimal.ZERO).divide(annuity, 0, RoundingMode.CEILING);
        BigDecimal increase = requiredMonthly.subtract(monthly).max(BigDecimal.ZERO);
        BigDecimal decrease = monthly.subtract(requiredMonthly).max(BigDecimal.ZERO);
        BigDecimal projected = futureValue(present, monthly, factor, months).setScale(0, RoundingMode.FLOOR);
        BigDecimal reduction = target.subtract(projected).max(BigDecimal.ZERO);
        BigDecimal surplus = projected.subtract(target).max(BigDecimal.ZERO);
        String assumption = "연 " + annualPercent.toPlainString()
                + "% 수익률 가정(임시 위험구간 기준, 월말 납입, 세금·수수료 제외)";

        String contribution;
        if (increase.signum() > 0) {
            contribution = "월 납입액을 " + won(monthly) + "원에서 " + won(requiredMonthly) + "원으로 "
                    + won(increase) + "원 늘리면 현재 목표일까지 목표금액에 도달하는 계산입니다. " + assumption + ".";
        } else if (decrease.signum() > 0) {
            contribution = "목표일과 목표금액 유지 시 월 납입액을 " + won(monthly) + "원에서 " + won(requiredMonthly) + "원으로 "
                    + won(decrease) + "원 줄여 자금 여유를 확보할 수 있습니다. " + assumption + ".";
        } else {
            contribution = "현재 월 납입액 " + won(monthly) + "원이면 계산상 목표에 도달하므로 증액이 필요하지 않습니다. " + assumption + ".";
        }

        String extension;
        if (projected.compareTo(target) >= 0) {
            int shortenedMonths = months;
            for (int m = 1; m <= months; m++) {
                if (futureValue(present, monthly, factor, m).compareTo(target) >= 0) {
                    shortenedMonths = m;
                    break;
                }
            }
            if (shortenedMonths < months) {
                int saved = months - shortenedMonths;
                extension = "월 납입액 " + won(monthly) + "원 유지 시 목표기간을 " + saved
                        + "개월 단축한 총 " + shortenedMonths + "개월(목표일 "
                        + goal.targetDate().minusMonths(saved) + ")에 조기 달성 가능합니다. " + assumption + ".";
            } else {
                extension = "월 납입액 " + won(monthly) + "원 유지 시 기존 목표기간 " + months
                        + "개월(목표일 " + goal.targetDate() + ") 내에 달성 가능합니다. " + assumption + ".";
            }
        } else {
            int requiredMonths = months;
            BigDecimal future = projected;
            while (future.compareTo(target) < 0 && requiredMonths < MAX_MONTHS) {
                future = future.multiply(factor, MC).add(monthly, MC);
                requiredMonths++;
            }
            extension = future.compareTo(target) >= 0
                    ? "월 납입액 " + won(monthly) + "원을 유지하면 목표기간을 " + (requiredMonths - months)
                        + "개월 연장한 총 " + requiredMonths + "개월(목표일 "
                        + goal.targetDate().plusMonths(requiredMonths - months) + ")에 계산상 도달합니다. " + assumption + "."
                    : "월 납입액 " + won(monthly) + "원을 유지하면 총 " + MAX_MONTHS
                        + "개월 이내에는 계산상 목표에 도달하지 못합니다. 기간 연장만으로 부족하며 납입액 조정이 필요합니다. " + assumption + ".";
        }

        String targetAdjustment;
        if (projected.signum() == 0) {
            targetAdjustment = "현재 자금계획의 목표일 예상금액은 0원입니다. 목표금액을 줄이는 것만으로는 양수 목표를 달성할 수 없어 자금 마련이 먼저 필요합니다.";
        } else if (reduction.signum() > 0) {
            targetAdjustment = "기간과 납입액을 유지하려면 목표금액을 " + won(target) + "원에서 " + won(projected)
                    + "원으로 " + won(reduction) + "원 낮추는 안을 검토하세요. " + assumption + ".";
        } else if (surplus.signum() > 0) {
            targetAdjustment = "기간과 납입액 유지 시 목표일 예상금액은 " + won(projected) + "원으로, 기존 목표 대비 "
                    + won(surplus) + "원 상향한 " + won(projected) + "원으로 목표금액을 조절할 수 있습니다. " + assumption + ".";
        } else {
            targetAdjustment = "기간과 납입액 유지 시 목표일 예상금액은 " + won(projected) + "원으로, 목표금액 감액은 필요하지 않습니다. " + assumption + ".";
        }

        String split = reduction.signum() > 0 && projected.signum() > 0
                ? "목표일 예상금액 " + won(projected) + "원을 기준으로 1차 목표를 검토하고, 부족한 "
                    + won(reduction) + "원은 추가 목표로 분리할 수 있습니다. 실제 필수목표 금액은 두 사람이 정해야 합니다."
                : "목표일 예상금액은 " + won(projected) + "원이며 목표 달성에 충분합니다.";

        String comparison = "대안 비교: 납입액 조정은 목표일 유지, 기간 조정은 월 납입액 유지, 목표금액 조정은 현재 자금계획 유지입니다. "
                + assumption + "에 따른 예상금액 비교이며, 목표 달성 확률이나 수익을 보장하지 않습니다.";

        return List.of(contribution, extension, targetAdjustment, split, comparison);
    }

    private BigDecimal assumedAnnualPercent(Recommendation r) {
        BigDecimal risk = r.centerScore().min(BigDecimal.valueOf(r.finalMax()));
        // 기존 G 구간의 하한이 R 이하인 구간 중 최대 허용 수익률. R=0은 무수익 가정.
        int rate = risk.signum() <= 0 ? 0 : risk.compareTo(BigDecimal.valueOf(20)) < 0 ? 2
                : risk.compareTo(BigDecimal.valueOf(35)) < 0 ? 4
                : risk.compareTo(BigDecimal.valueOf(50)) < 0 ? 6
                : risk.compareTo(BigDecimal.valueOf(65)) < 0 ? 8
                : risk.compareTo(BigDecimal.valueOf(80)) < 0 ? 12 : 20;
        return BigDecimal.valueOf(rate);
    }

    private BigDecimal monthlyFactor(BigDecimal annualPercent) {
        if (annualPercent.signum() == 0) return BigDecimal.ONE;
        BigDecimal annualFactor = BigDecimal.ONE.add(annualPercent.movePointLeft(2));
        BigDecimal low = BigDecimal.ONE;
        BigDecimal high = annualFactor;
        for (int i = 0; i < 100; i++) {
            BigDecimal mid = low.add(high).divide(BigDecimal.valueOf(2), MC);
            if (mid.pow(12, MC).compareTo(annualFactor) <= 0) low = mid; else high = mid;
        }
        return low;
    }

    private BigDecimal annuity(BigDecimal factor, int months) {
        if (factor.compareTo(BigDecimal.ONE) == 0) return BigDecimal.valueOf(months);
        return factor.pow(months, MC).subtract(BigDecimal.ONE)
                .divide(factor.subtract(BigDecimal.ONE), MC);
    }

    private BigDecimal futureValue(BigDecimal present, BigDecimal monthly, BigDecimal factor, int months) {
        return present.multiply(factor.pow(months, MC), MC).add(monthly.multiply(annuity(factor, months), MC), MC);
    }

    private String won(BigDecimal value) {
        return String.format(Locale.KOREA, "%,.0f", value);
    }
}
