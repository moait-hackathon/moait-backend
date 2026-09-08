package com.moait.moai.domain.analysis.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.moait.moai.domain.analysis.dto.JointRiskAssessmentRequestDTO;
import com.moait.moai.domain.analysis.dto.InvestmentAgreementResponseDTO.GoalRequirement;
import com.moait.moai.domain.analysis.dto.InvestmentAgreementResponseDTO.Recommendation;
import com.moait.moai.domain.analysis.dto.RiskAssessmentRequestDTO.InvestmentExperience;
import com.moait.moai.domain.analysis.dto.RiskAssessmentRequestDTO.LossReaction;
import com.moait.moai.domain.analysis.dto.RiskAssessmentRequestDTO.LossTolerance;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.ObjectProvider;

class AgreementRulesTest {
    @Test
    void doesNotRoundRBeforeComparingDifference() {
        assertThat(InvestmentAnalysisServiceImpl.determineStatus(
                new Recommendation(new BigDecimal("50.2"), new BigDecimal("50.2"), 45, 55, 100),
                new GoalRequirement(8.0, 65, 80, true, 60, "LEGACY_RETURN_BANDS")))
                .isEqualTo("CONDITIONAL");
    }

    @ParameterizedTest
    @CsvSource({"-16,LOWER_RISK_SUFFICIENT", "-15,LOWER_RISK_SUFFICIENT",
            "-14,GOAL_INCREASE_POSSIBLE", "-6,GOAL_INCREASE_POSSIBLE",
            "-5,SUITABLE", "0,SUITABLE", "5,SUITABLE", "6,CONDITIONAL",
            "14,CONDITIONAL", "15,UNSUITABLE", "16,UNSUITABLE"})
    void comparesExactDifferenceBoundaries(int difference, String status) {
        assertThat(InvestmentAnalysisServiceImpl.determineStatus(
                new Recommendation(BigDecimal.valueOf(50), BigDecimal.valueOf(50), 45, 55, 100),
                new GoalRequirement(5.0, 50 + difference, 80, true, 60, "LEGACY_RETURN_BANDS")))
                .isEqualTo(status);
    }

    @ParameterizedTest
    @CsvSource({"11,0", "12,3", "23,3", "24,6", "35,6", "36,9",
            "59,9", "60,12", "119,12", "120,15"})
    void scoresActualGoalMonths(int months, int expected) {
        assertThat(InvestmentAnalysisServiceImpl.scoreJoint(joint("0", "0", 0), months)
                .preferenceScore()).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({"101,0", "100,0", "99.99,4", "90.01,4", "90,8",
            "80.01,8", "80,13", "70.01,13", "70,17", "0,17"})
    void comparesSurplusWithoutRounding(String expense, int expected) {
        assertThat(InvestmentAnalysisServiceImpl.scoreJoint(joint("100", expense, 0), 1)
                .preferenceScore()).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({"0,0", "1,4", "2,4", "3,9", "5,9", "6,14", "11,14", "12,18"})
    void scoresEmergencyBoundaries(int months, int expected) {
        assertThat(InvestmentAnalysisServiceImpl.scoreJoint(joint("0", "0", months), 1)
                .preferenceScore()).isEqualTo(expected);
    }

    @Test
    void maximumJointAnswersTotalOneHundred() {
        var q = new JointRiskAssessmentRequestDTO(LossTolerance.OVER_30, LossReaction.BUY_MORE,
                12, BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.ZERO, InvestmentExperience.HIGH_RISK);
        assertThat(InvestmentAnalysisServiceImpl.scoreJoint(q, 120).preferenceScore()).isEqualTo(100);
    }

    @Test
    @SuppressWarnings("unchecked")
    void fallbackStopsRecommendationAndNeverRaisesRiskAboveGoal() {
        var generator = new SpringAiAgreementGenerator(mock(ObjectProvider.class));
        var r = new Recommendation(BigDecimal.valueOf(50), BigDecimal.valueOf(50), 45, 55, 100);
        var g = new GoalRequirement(0.0, 0, 20, true, 24, "LEGACY_RETURN_BANDS");
        var stopped = generator.generate(r, g, null, "UNSUITABLE");
        assertThat(stopped.recommendedRiskScore()).isNull();
        assertThat(stopped.recommendedStrategy()).isNull();
        assertThat(stopped.alternatives()).hasSize(5);
        assertThat(generator.generate(r, g, null, "LOWER_RISK_SUFFICIENT").recommendedRiskScore()).isZero();
        assertThat(generator.generate(r, g, null, "CONDITIONAL").alternatives()).hasSize(5);
    }

    private JointRiskAssessmentRequestDTO joint(String income, String expenses, int emergency) {
        return new JointRiskAssessmentRequestDTO(LossTolerance.NO_LOSS, LossReaction.SELL_ALL,
                emergency, new BigDecimal(income), new BigDecimal(expenses), BigDecimal.ZERO,
                InvestmentExperience.NONE);
    }
}
