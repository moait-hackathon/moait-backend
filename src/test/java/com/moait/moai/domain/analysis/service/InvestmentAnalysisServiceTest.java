package com.moait.moai.domain.analysis.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.moait.moai.domain.analysis.dto.GoalAnalysisRequestDTO;
import com.moait.moai.domain.analysis.dto.InvestmentAgreementRequestDTO;
import com.moait.moai.domain.analysis.dto.InvestmentAgreementResponseDTO.Agreement;
import com.moait.moai.domain.analysis.dto.InvestmentAnalysisInputDTO;
import com.moait.moai.domain.analysis.dto.InvestmentAnalysisInputDTO.AssetPosition;
import com.moait.moai.domain.analysis.dto.InvestmentAnalysisInputDTO.EmergencyFundBand;
import com.moait.moai.domain.analysis.dto.InvestmentAnalysisInputDTO.InvestmentExperience;
import com.moait.moai.domain.analysis.dto.InvestmentAnalysisInputDTO.JointFund;
import com.moait.moai.domain.analysis.dto.InvestmentAnalysisInputDTO.LossReaction;
import com.moait.moai.domain.analysis.dto.InvestmentAnalysisInputDTO.SurplusBand;
import com.moait.moai.domain.analysis.exception.InvestmentAnalysisDataException;
import com.moait.moai.domain.report.entity.InvestmentReport;
import com.moait.moai.domain.report.repository.InvestmentReportRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InvestmentAnalysisServiceTest {
    @Mock
    private InvestmentAnalysisInputService inputService;
    @Mock
    private InvestmentReportRepository reportRepository;
    @Mock
    private AgreementGenerator generator;
    private InvestmentAnalysisServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new InvestmentAnalysisServiceImpl(generator, reportRepository, inputService,
                new PortfolioRiskCalculator());
    }

    @Test
    void usesBothPortfoliosAndJointSurveyThenStoresDatabaseGoalSnapshot() {
        var goal = new GoalAnalysisRequestDTO(200000000L, 100000000L, 3000000L,
                LocalDate.now().plusMonths(24));
        var joint = new JointFund(10, LossReaction.HOLD, EmergencyFundBand.M6_12,
                SurplusBand.B20_30, InvestmentExperience.ETF_ONLY);
        var input = new InvestmentAnalysisInputDTO(301L, goal, joint,
                List.of(new AssetPosition("STOCK", "HIGH", BigDecimal.TEN, "KRW")),
                List.of(new AssetPosition("BOND", "LOW", BigDecimal.TEN, "KRW")));
        when(inputService.load(101L)).thenReturn(input);
        when(generator.generate(any(), any(), eq(goal), any())).thenReturn(
                new Agreement("SUITABLE", 35, "요약", "근거", "전략", List.of("대안"), List.of(), false));

        var result = service.analyze(new InvestmentAgreementRequestDTO(101L));

        assertThat(result.personA().preferenceScore()).isEqualTo(75);
        assertThat(result.personB().preferenceScore()).isEqualTo(25);
        assertThat(result.jointFund().preferenceScore()).isEqualTo(61);
        assertThat(result.recommendation().weightedScore()).isEqualByComparingTo("56.6");
        assertThat(result.recommendation().centerScore()).isEqualByComparingTo("45");
        assertThat(result.recommendation().finalMax()).isEqualTo(45);
        assertThat(result.goalRequirement().investmentMonths()).isEqualTo(24);
        var report = ArgumentCaptor.forClass(InvestmentReport.class);
        verify(reportRepository).save(report.capture());
        assertThat(report.getValue().getGoalId()).isEqualTo(301L);
        assertThat(report.getValue().getInputMonthlyContribution()).isEqualTo(3000000L);
        assertThat(report.getValue().getInputTargetDate()).isEqualTo(goal.targetDate());
    }

    @Test
    void missingPartnerAssetsStopsBeforeAiAndSave() {
        var goal = new GoalAnalysisRequestDTO(100L, 10L, 1L, LocalDate.now().plusMonths(24));
        when(inputService.load(101L)).thenReturn(new InvestmentAnalysisInputDTO(301L, goal, null,
                List.of(new AssetPosition("STOCK", "HIGH", BigDecimal.TEN, "KRW")), List.of()));
        assertThatThrownBy(() -> service.analyze(new InvestmentAgreementRequestDTO(101L)))
                .isInstanceOf(InvestmentAnalysisDataException.class).hasMessageContaining("개인 B");
        verifyNoInteractions(generator, reportRepository);
    }
}
