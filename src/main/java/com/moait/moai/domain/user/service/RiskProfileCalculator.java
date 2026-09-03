package com.moait.moai.domain.user.service;

import com.moait.moai.common.enums.InvestmentExperience;
import com.moait.moai.common.enums.InvestmentHorizon;
import com.moait.moai.common.enums.LossReaction;
import com.moait.moai.common.enums.RiskProfileType;
import org.springframework.stereotype.Component;

/**
 * 투자성향 설문 응답 → 점수(0~100) → {@link RiskProfileType} 분류.
 *
 * <p>배점표는 {@code docs/api-spec.md} "투자성향 점수 산출" 참고. MVP 초기 가설이며 상수만 조정한다.
 *
 * <pre>
 * 총점 100 = Q3 최대감내손실률(35) + Q2 손실대응(25) + Q4 투자기간(20) + Q1 투자경험(12) + Q7 비상자금(8)
 * </pre>
 */
@Component
public class RiskProfileCalculator {

    public Result calculate(InvestmentExperience experience,
                            LossReaction lossReaction,
                            int maxTolerableLossRate,
                            InvestmentHorizon horizon,
                            boolean emergencyFundSecured) {
        int score = scoreMaxLossRate(maxTolerableLossRate)
                + scoreLossReaction(lossReaction)
                + scoreHorizon(horizon)
                + scoreExperience(experience)
                + (emergencyFundSecured ? 8 : 0);
        return new Result(classify(score), score);
    }

    public record Result(RiskProfileType type, int score) {
    }

    /** Q3 — 최대 감내 손실률(%) : 35점 */
    private int scoreMaxLossRate(int rate) {
        if (rate <= 0) {
            return 0;
        }
        if (rate <= 5) {
            return 8;
        }
        if (rate <= 10) {
            return 15;
        }
        if (rate <= 20) {
            return 25;
        }
        if (rate <= 30) {
            return 32;
        }
        return 35;
    }

    /** Q2 — 손실 시 대응 : 25점 */
    private int scoreLossReaction(LossReaction reaction) {
        return switch (reaction) {
            case SELL_ALL -> 0;
            case SELL_PART -> 8;
            case HOLD -> 18;
            case BUY_MORE -> 25;
        };
    }

    /** Q4 — 투자 가능 기간 : 20점 */
    private int scoreHorizon(InvestmentHorizon horizon) {
        return switch (horizon) {
            case UNDER_1Y -> 0;
            case Y1_3 -> 7;
            case Y3_5 -> 14;
            case OVER_5Y -> 20;
        };
    }

    /** Q1 — 투자 경험 : 12점 */
    private int scoreExperience(InvestmentExperience experience) {
        return switch (experience) {
            case NONE -> 0;
            case SAVINGS_ONLY, ETC -> 3;
            case ETF_ONLY -> 7;
            case STOCK_ALL -> 12;
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
