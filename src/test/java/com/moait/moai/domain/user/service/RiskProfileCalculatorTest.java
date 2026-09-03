package com.moait.moai.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.moait.moai.common.enums.InvestmentExperience;
import com.moait.moai.common.enums.InvestmentHorizon;
import com.moait.moai.common.enums.LossReaction;
import com.moait.moai.common.enums.RiskProfileType;
import com.moait.moai.domain.user.service.RiskProfileCalculator.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RiskProfileCalculatorTest {

    private final RiskProfileCalculator calculator = new RiskProfileCalculator();

    @Test
    @DisplayName("모든 문항 최저 → 0점 안정형")
    void minScore() {
        Result r = calculator.calculate(
                InvestmentExperience.NONE, LossReaction.SELL_ALL, 0,
                InvestmentHorizon.UNDER_1Y, false);

        assertThat(r.score()).isZero();
        assertThat(r.type()).isEqualTo(RiskProfileType.STABLE);
    }

    @Test
    @DisplayName("모든 문항 최고 → 100점 공격투자형")
    void maxScore() {
        Result r = calculator.calculate(
                InvestmentExperience.STOCK_ALL, LossReaction.BUY_MORE, 40,
                InvestmentHorizon.OVER_5Y, true);

        assertThat(r.score()).isEqualTo(100); // 35 + 25 + 20 + 12 + 8
        assertThat(r.type()).isEqualTo(RiskProfileType.AGGRESSIVE);
    }

    @Test
    @DisplayName("중간 조합 → 44점 위험중립형")
    void neutral() {
        // ETF_ONLY(7) + SELL_PART(8) + 10%(15) + Y3_5(14) + false(0) = 44
        Result r = calculator.calculate(
                InvestmentExperience.ETF_ONLY, LossReaction.SELL_PART, 10,
                InvestmentHorizon.Y3_5, false);

        assertThat(r.score()).isEqualTo(44);
        assertThat(r.type()).isEqualTo(RiskProfileType.NEUTRAL);
    }

    @Test
    @DisplayName("적극투자형 조합 → 77점")
    void active() {
        // STOCK_ALL(12) + HOLD(18) + 20%(25) + Y3_5(14) + true(8) = 77
        Result r = calculator.calculate(
                InvestmentExperience.STOCK_ALL, LossReaction.HOLD, 20,
                InvestmentHorizon.Y3_5, true);

        assertThat(r.score()).isEqualTo(77);
        assertThat(r.type()).isEqualTo(RiskProfileType.ACTIVE);
    }

    @Test
    @DisplayName("STABLE / STABLE_SEEKING 경계 근처 분류")
    void classifyLowRange() {
        // 19점 → STABLE : SAVINGS_ONLY(3) + SELL_PART(8) + 1~5%(8) + UNDER_1Y(0) + false(0)
        Result score19 = calculator.calculate(
                InvestmentExperience.SAVINGS_ONLY, LossReaction.SELL_PART, 5,
                InvestmentHorizon.UNDER_1Y, false);
        assertThat(score19.score()).isEqualTo(19);
        assertThat(score19.type()).isEqualTo(RiskProfileType.STABLE);

        // 27점 → STABLE_SEEKING : 위 + 비상자금(8)
        Result score27 = calculator.calculate(
                InvestmentExperience.SAVINGS_ONLY, LossReaction.SELL_PART, 5,
                InvestmentHorizon.UNDER_1Y, true);
        assertThat(score27.score()).isEqualTo(27);
        assertThat(score27.type()).isEqualTo(RiskProfileType.STABLE_SEEKING);
    }
}
