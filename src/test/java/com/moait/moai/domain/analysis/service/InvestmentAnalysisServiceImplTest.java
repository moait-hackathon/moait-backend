package com.moait.moai.domain.analysis.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.moait.moai.domain.analysis.dto.GoalAnalysisRequestDTO;
import com.moait.moai.domain.analysis.dto.InvestmentAgreementRequestDTO;
import com.moait.moai.domain.analysis.dto.InvestmentAgreementResponseDTO;
import com.moait.moai.domain.analysis.dto.InvestmentAgreementResponseDTO.Agreement;
import com.moait.moai.domain.analysis.dto.RiskAssessmentRequestDTO;
import com.moait.moai.domain.analysis.dto.RiskAssessmentRequestDTO.CapitalProtection;
import com.moait.moai.domain.analysis.dto.RiskAssessmentRequestDTO.IncomeStability;
import com.moait.moai.domain.analysis.dto.RiskAssessmentRequestDTO.InvestmentExperience;
import com.moait.moai.domain.analysis.dto.RiskAssessmentRequestDTO.InvestmentHorizon;
import com.moait.moai.domain.analysis.dto.RiskAssessmentRequestDTO.LossReaction;
import com.moait.moai.domain.analysis.dto.RiskAssessmentRequestDTO.LossTolerance;
import com.moait.moai.domain.analysis.dto.RiskAssessmentRequestDTO.PlannedExpense;
import com.moait.moai.domain.analysis.dto.RiskAssessmentRequestDTO.PsychologicalBurden;
import com.moait.moai.domain.analysis.dto.RiskAssessmentRequestDTO.WithdrawalPlan;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class InvestmentAnalysisServiceImplTest {

    private final AgreementGenerator generator = (r, g, goal, status) ->
            new Agreement(status, Math.min(r.finalMax(), g.rangeMin()), "summary",
                    "rationale", List.of(), List.of(), false);
    private final InvestmentAnalysisService service = new InvestmentAnalysisServiceImpl(generator);

    @Test
    void appliesMostConservativeLimitAndRejectsGoalAboveIt() {
        RiskAssessmentRequestDTO aggressive = assessment(LossTolerance.OVER_30,
                CapitalProtection.BELOW_80, LossReaction.BUY_MORE, InvestmentHorizon.OVER_10Y);
        RiskAssessmentRequestDTO conservative = assessment(LossTolerance.UP_TO_10,
                CapitalProtection.P90, LossReaction.SELL_PART, InvestmentHorizon.Y3_5);
        RiskAssessmentRequestDTO joint = assessment(LossTolerance.UP_TO_20,
                CapitalProtection.P80, LossReaction.HOLD, InvestmentHorizon.Y5_10);
        GoalAnalysisRequestDTO goal = new GoalAnalysisRequestDTO(
                200_000_000L, 100_000_000L, 3_000_000L, 0L, 0L, 0L,
                LocalDate.now().plusMonths(24));

        InvestmentAgreementResponseDTO result = service.analyze(
                new InvestmentAgreementRequestDTO(aggressive, conservative, joint, goal));

        assertThat(result.personB().finalLimit()).isEqualTo(45);
        assertThat(result.recommendation().finalMax()).isEqualTo(45);
        assertThat(result.recommendation().rangeMax()).isLessThanOrEqualTo(45);
        assertThat(result.goalRequirement().rangeMin()).isGreaterThan(45);
        assertThat(result.agreement().status()).isEqualTo("UNSUITABLE");
    }

    @Test
    void recommendsLowerRiskWhenContributionsAlreadyReachGoal() {
        RiskAssessmentRequestDTO aggressive = assessment(LossTolerance.OVER_30,
                CapitalProtection.BELOW_80, LossReaction.BUY_MORE, InvestmentHorizon.OVER_10Y);
        GoalAnalysisRequestDTO goal = new GoalAnalysisRequestDTO(
                120_000_000L, 100_000_000L, 3_000_000L, 0L, 0L, 0L,
                LocalDate.now().plusMonths(24));

        InvestmentAgreementResponseDTO result = service.analyze(
                new InvestmentAgreementRequestDTO(aggressive, aggressive, aggressive, goal));

        assertThat(result.goalRequirement().requiredAnnualReturnRate()).isZero();
        assertThat(result.goalRequirement().rangeMin()).isZero();
        assertThat(result.agreement().status()).isEqualTo("LOWER_RISK_SUFFICIENT");
    }

    private RiskAssessmentRequestDTO assessment(LossTolerance loss,
            CapitalProtection capital, LossReaction reaction, InvestmentHorizon horizon) {
        return new RiskAssessmentRequestDTO(loss, capital, reaction,
                PsychologicalBurden.TOLERATE_20, 12, IncomeStability.MULTIPLE_STABLE,
                10.0, 10.0, PlannedExpense.NONE, horizon, WithdrawalPlan.NONE,
                InvestmentExperience.HIGH_RISK, 5);
    }
}
