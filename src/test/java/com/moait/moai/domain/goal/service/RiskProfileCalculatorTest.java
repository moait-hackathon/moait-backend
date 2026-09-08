package com.moait.moai.domain.goal.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.moait.moai.common.enums.EmergencyFundMonths;
import com.moait.moai.common.enums.InvestmentExperience;
import com.moait.moai.common.enums.LossReaction;
import com.moait.moai.common.enums.MonthlySurplusBand;
import com.moait.moai.common.enums.RiskProfileType;
import com.moait.moai.domain.goal.service.RiskProfileCalculator.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RiskProfileCalculatorTest {

    private final RiskProfileCalculator calculator = new RiskProfileCalculator();

    @Test
    @DisplayName("모든 문항 최저 → 0점 안정형")
    void minScore() {
        Result r = calculator.calculate(
                0, 0, LossReaction.SELL_ALL,
                EmergencyFundMonths.UNDER_1M, MonthlySurplusBand.UNDER_0, InvestmentExperience.NONE);

        assertThat(r.score()).isZero();
        assertThat(r.type()).isEqualTo(RiskProfileType.STABLE);
    }

    @Test
    @DisplayName("모든 문항 최고 → 100점 공격투자형")
    void maxScore() {
        Result r = calculator.calculate(
                120, 40, LossReaction.BUY_MORE,
                EmergencyFundMonths.OVER_12M, MonthlySurplusBand.OVER_30, InvestmentExperience.MULTI_ASSET);

        assertThat(r.score()).isEqualTo(100); // 25 + 15 + 18 + 17 + 15 + 10
        assertThat(r.type()).isEqualTo(RiskProfileType.AGGRESSIVE);
    }

    @Test
    @DisplayName("배점표대로 합산 — 60개월/손실10/HOLD/M3_6/B20_30/ETF_ONLY = 62")
    void rubricSum() {
        // 투자기간 60(12) + 최대손실 10(12) + HOLD(11) + M3_6(9) + B20_30(13) + ETF_ONLY(5) = 62
        Result r = calculator.calculate(
                60, 10, LossReaction.HOLD,
                EmergencyFundMonths.M3_6, MonthlySurplusBand.B20_30, InvestmentExperience.ETF_ONLY);

        assertThat(r.score()).isEqualTo(62);
        assertThat(r.type()).isEqualTo(RiskProfileType.ACTIVE); // 61~80
    }

    @Test
    @DisplayName("투자기간 점수 구간 경계")
    void periodBands() {
        assertThat(scoreForPeriod(11)).isEqualTo(0);
        assertThat(scoreForPeriod(12)).isEqualTo(3);
        assertThat(scoreForPeriod(23)).isEqualTo(3);
        assertThat(scoreForPeriod(24)).isEqualTo(6);
        assertThat(scoreForPeriod(59)).isEqualTo(9);
        assertThat(scoreForPeriod(60)).isEqualTo(12);
        assertThat(scoreForPeriod(120)).isEqualTo(15);
    }

    /** 투자기간만 변화시키고 나머지 0점 조합으로 고정 → 순수 기간 점수 추출. */
    private int scoreForPeriod(int months) {
        return calculator.calculate(months, 0, LossReaction.SELL_ALL,
                EmergencyFundMonths.UNDER_1M, MonthlySurplusBand.UNDER_0, InvestmentExperience.NONE).score();
    }

    @Test
    @DisplayName("R 점수 → 유형 5구간 분류")
    void classify() {
        // 20 → STABLE : 최대손실 20(19) + SELL_MOST(3) = 22... 경계 확인용으로 조합 구성
        assertThat(calculator.calculate(0, 5, LossReaction.SELL_ALL,
                EmergencyFundMonths.UNDER_1M, MonthlySurplusBand.UNDER_0, InvestmentExperience.NONE)
                .type()).isEqualTo(RiskProfileType.STABLE); // 6점
        assertThat(calculator.calculate(0, 30, LossReaction.HOLD,
                EmergencyFundMonths.UNDER_1M, MonthlySurplusBand.UNDER_0, InvestmentExperience.NONE)
                .type()).isEqualTo(RiskProfileType.STABLE_SEEKING); // 23 + 11 = 34
    }
}
