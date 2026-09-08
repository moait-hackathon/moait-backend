package com.moait.moai.domain.goal.service;

import com.moait.moai.common.enums.EmergencyFundMonths;
import com.moait.moai.common.enums.InvestmentExperience;
import com.moait.moai.common.enums.LossReaction;
import com.moait.moai.common.enums.MonthlySurplusBand;
import com.moait.moai.common.enums.RiskProfileType;
import org.springframework.stereotype.Component;

/**
 * 온보딩 6문항 → 공동 위험점수 R(0~100) → {@link RiskProfileType} 분류.
 *
 * <p>배점표는 {@code docs/api-spec.md} "온보딩 점수 산출" 참고. MVP 초기 가설이며 상수만 조정한다.
 *
 * <pre>
 * 총점 100 = 최대허용손실(25) + 급락행동(15) + 비상자금(18) + 월여유자금(17) + 투자기간(15) + 투자경험(10)
 * </pre>
 */
@Component
public class RiskProfileCalculator {

    public Result calculate(int investmentPeriodMonths,
                            int maxAllowedLossRate,
                            LossReaction lossReaction,
                            EmergencyFundMonths emergencyFundMonths,
                            MonthlySurplusBand monthlySurplusBand,
                            InvestmentExperience investmentExperience) {
        int score = scoreMaxLoss(maxAllowedLossRate)
                + scoreLossReaction(lossReaction)
                + scoreEmergencyFund(emergencyFundMonths)
                + scoreSurplus(monthlySurplusBand)
                + scorePeriod(investmentPeriodMonths)
                + scoreExperience(investmentExperience);
        return new Result(score, classify(score));
    }

    public record Result(int score, RiskProfileType type) {
    }

    /** 최대 허용손실(%) : 25점 — 0/5/10/20/30/40 만 유효. */
    private int scoreMaxLoss(int rate) {
        return switch (rate) {
            case 0 -> 0;
            case 5 -> 6;
            case 10 -> 12;
            case 20 -> 19;
            case 30 -> 23;
            case 40 -> 25;
            default -> throw new IllegalArgumentException("maxAllowedLossRate: " + rate);
        };
    }

    /** 급락 시 행동 : 15점. */
    private int scoreLossReaction(LossReaction reaction) {
        return switch (reaction) {
            case SELL_ALL -> 0;
            case SELL_MOST -> 3;
            case SELL_PART -> 7;
            case HOLD -> 11;
            case BUY_MORE -> 15;
        };
    }

    /** 비상자금 : 18점. */
    private int scoreEmergencyFund(EmergencyFundMonths months) {
        return switch (months) {
            case UNDER_1M -> 0;
            case M1_3 -> 4;
            case M3_6 -> 9;
            case M6_12 -> 14;
            case OVER_12M -> 18;
        };
    }

    /** 월 여유자금 비율 : 17점. */
    private int scoreSurplus(MonthlySurplusBand band) {
        return switch (band) {
            case UNDER_0 -> 0;
            case UNDER_10 -> 4;
            case B10_20 -> 8;
            case B20_30 -> 13;
            case OVER_30 -> 17;
        };
    }

    /** 투자기간(목표일 파생 개월수) : 15점. */
    private int scorePeriod(int months) {
        if (months < 12) {
            return 0;
        }
        if (months < 24) {
            return 3;
        }
        if (months < 36) {
            return 6;
        }
        if (months < 60) {
            return 9;
        }
        if (months < 120) {
            return 12;
        }
        return 15;
    }

    /** 투자 경험·지식 : 10점. */
    private int scoreExperience(InvestmentExperience experience) {
        return switch (experience) {
            case NONE -> 0;
            case SAVINGS_ONLY -> 2;
            case ETF_ONLY -> 5;
            case STOCK_ALL -> 8;
            case MULTI_ASSET -> 10;
        };
    }

    private RiskProfileType classify(int score) {
        if (score <= 20) {
            return RiskProfileType.STABLE;
        }
        if (score <= 40) {
            return RiskProfileType.STABLE_SEEKING;
        }
        if (score <= 60) {
            return RiskProfileType.NEUTRAL;
        }
        if (score <= 80) {
            return RiskProfileType.ACTIVE;
        }
        return RiskProfileType.AGGRESSIVE;
    }
}
