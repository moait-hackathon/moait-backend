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
import com.moait.moai.domain.analysis.dto.JointRiskAssessmentRequestDTO;
import com.moait.moai.domain.report.repository.InvestmentReportRepository;
import java.math.BigDecimal;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import org.mockito.ArgumentCaptor;
import com.moait.moai.domain.report.entity.InvestmentReport;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.moait.moai.common.exception.BusinessException;
import com.moait.moai.common.exception.ErrorCode;

class InvestmentAnalysisServiceImplTest {

    private final AgreementGenerator generator = (r, g, goal, status) ->
            new Agreement(status, Math.min(r.finalMax(), g.rangeMin()), "summary",
                    "rationale", "strategy", List.of(), List.of(), false);
    private final InvestmentReportRepository repository = mock(InvestmentReportRepository.class);
    private final InvestmentAnalysisService service = new InvestmentAnalysisServiceImpl(generator, repository);

    @BeforeEach
    void allowJointGoal() {
        when(repository.countAccessibleGoal(10L, 1L)).thenReturn(1L);
    }

    @Test
    void appliesMostConservativeLimitAndRejectsGoalAboveIt() {
        RiskAssessmentRequestDTO aggressive = assessment(LossTolerance.OVER_30,
                CapitalProtection.BELOW_80, LossReaction.BUY_MORE, InvestmentHorizon.OVER_10Y);
        RiskAssessmentRequestDTO conservative = assessment(LossTolerance.UP_TO_10,
                CapitalProtection.P90, LossReaction.SELL_PART, InvestmentHorizon.Y3_5);
        GoalAnalysisRequestDTO goal = new GoalAnalysisRequestDTO(
                200_000_000L, 100_000_000L, 3_000_000L,
                LocalDate.now().plusMonths(24));

        InvestmentAgreementResponseDTO result = service.analyze(1L,
                new InvestmentAgreementRequestDTO(10L, aggressive, conservative, joint(), goal));

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
                120_000_000L, 100_000_000L, 3_000_000L,
                LocalDate.now().plusMonths(24));

        InvestmentAgreementResponseDTO result = service.analyze(1L,
                new InvestmentAgreementRequestDTO(10L, aggressive, aggressive, joint(), goal));

        assertThat(result.goalRequirement().requiredAnnualReturnRate()).isZero();
        assertThat(result.goalRequirement().rangeMin()).isZero();
        assertThat(result.agreement().status()).isEqualTo("LOWER_RISK_SUFFICIENT");
        ArgumentCaptor<InvestmentReport> saved = ArgumentCaptor.forClass(InvestmentReport.class);
        verify(repository).save(saved.capture());
        assertThat(saved.getValue().getGoalId()).isEqualTo(10L);
        assertThat(saved.getValue().getInvestmentMonths()).isEqualTo(24);
        assertThat(saved.getValue().getRationale()).isEqualTo("rationale");
        assertThat(saved.getValue().getRecommendedStrategy()).isEqualTo("strategy");
        assertThat(saved.getValue().getInputTargetAmount()).isEqualTo(goal.targetAmount());
        assertThat(saved.getValue().getInputCurrentAmount()).isEqualTo(goal.currentAmount());
        assertThat(saved.getValue().getInputMonthlyContribution()).isEqualTo(goal.monthlyContribution());
        assertThat(saved.getValue().getInputTargetDate()).isEqualTo(goal.targetDate());
    }

    @Test
    void inaccessibleGoalIsRejectedBeforeGeneratingOrSaving() {
        when(repository.countAccessibleGoal(10L, 1L)).thenReturn(0L);
        AgreementGenerator unusedGenerator = mock(AgreementGenerator.class);
        var securedService = new InvestmentAnalysisServiceImpl(unusedGenerator, repository);
        var request = new InvestmentAgreementRequestDTO(10L, null, null, null, null);
        assertThatThrownBy(() -> securedService.analyze(1L, request))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND));
        verify(repository, never()).save(any());
        org.mockito.Mockito.verifyNoInteractions(unusedGenerator);
    }

    @Test
    void noFundingCannotProduceAFiniteRequiredReturnOrSuitableGoal() {
        var a = assessment(LossTolerance.OVER_30, CapitalProtection.BELOW_80,
                LossReaction.BUY_MORE, InvestmentHorizon.OVER_10Y);
        var goal = new GoalAnalysisRequestDTO(100L, 0L, 0L, LocalDate.now().plusMonths(24));
        var result = service.analyze(1L, new InvestmentAgreementRequestDTO(10L, a, a, joint(), goal));
        assertThat(result.goalRequirement().requiredAnnualReturnRate()).isNull();
        assertThat(result.goalRequirement().realistic()).isFalse();
        assertThat(result.agreement().status()).isEqualTo("UNSUITABLE");
    }

    private JointRiskAssessmentRequestDTO joint() {
        return new JointRiskAssessmentRequestDTO(LossTolerance.OVER_30, LossReaction.BUY_MORE,
                12, new BigDecimal("100"), BigDecimal.ZERO, BigDecimal.ZERO, InvestmentExperience.HIGH_RISK);
    }

    @Test
    void extremeRequiredReturnDoesNotOverflowDuringDisplayRounding() {
        var a = assessment(LossTolerance.OVER_30, CapitalProtection.BELOW_80,
                LossReaction.BUY_MORE, InvestmentHorizon.OVER_10Y);
        var goal = new GoalAnalysisRequestDTO(Long.MAX_VALUE, 1L, 0L, LocalDate.now().plusMonths(1));
        var result = service.analyze(1L, new InvestmentAgreementRequestDTO(10L, a, a, joint(), goal));
        assertThat(result.goalRequirement().requiredAnnualReturnRate()).isGreaterThan(1e100);
        assertThat(result.agreement().status()).isEqualTo("UNSUITABLE");
    }

    private RiskAssessmentRequestDTO assessment(LossTolerance loss,
            CapitalProtection capital, LossReaction reaction, InvestmentHorizon horizon) {
        return new RiskAssessmentRequestDTO(loss, capital, reaction,
                PsychologicalBurden.TOLERATE_20, 12, IncomeStability.MULTIPLE_STABLE,
                10.0, 10.0, PlannedExpense.NONE, horizon, WithdrawalPlan.NONE,
                InvestmentExperience.HIGH_RISK, 5);
    }
}
